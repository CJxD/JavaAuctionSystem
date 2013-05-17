package com.cjwatts.auctionsystem.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.cjwatts.auctionsystem.alert.Alerter;
import com.cjwatts.auctionsystem.message.Message;

public class Comms {

	private static InetAddress localAddr = InetAddress.getLoopbackAddress();
	private static InetAddress remoteAddr = localAddr;
	private static int receivePort = 7501;
	private static int transmitPort = 7500;

	private static int attemptCount = 0;
	private static final int attemptMax = 3;

	private static ServerSocket rxSock;
	private static boolean transientRxSock = false;
	private static Thread listener;
	private static ArrayList<CommsListener> listeners = new ArrayList<>();

	private static ConcurrentLinkedQueue<Socket> waiting = new ConcurrentLinkedQueue<>();

	/**
	 * Send a message to another system as defined by remoteAddr
	 * 
	 * @param m
	 * @return True if successful
	 */
	public synchronized static boolean sendMessage(Message m) {
		// Try 3 times
		boolean success = true;
		try (Socket txSock = new Socket(remoteAddr, transmitPort);
			ObjectOutputStream oos = new ObjectOutputStream(txSock.getOutputStream())) {
			
			oos.writeObject(m);
		} catch (IOException ex) {
			if (attemptCount < attemptMax - 1) {
				attemptCount++;
				success = sendMessage(m);
			} else {
				Alerter.getHandler().severe(
						"Could not contact server",
						"The server doesn't appear to be cooperating. "
								+ ex.getMessage());
				success = false;
			}
		} finally {
			// Reset attempt counter
			attemptCount = 0;
		}
		return success;
	}
	
	/**
	 * Send a message to another system
	 * 
	 * @param m
	 * @param destination
	 * @return True if successful
	 */
	public synchronized static boolean sendMessage(Message m, InetAddress destination) {
		InetAddress previous = getRemoteAddr();
		setRemoteAddr(destination);
		boolean success = sendMessage(m);
		setRemoteAddr(previous);
		return success;
	}

	/**
	 * Accept an incoming message from another system
	 * 
	 * @return Received message
	 */
	public synchronized static Message receiveMessage() {
		Message output = null;
		// If no clients are waiting, check for new ones
		if (waiting.isEmpty()) {
			try {
				// If no receiving socket exists, create a temporary one
				if (rxSock == null || rxSock.isClosed()) {
					rxSock = new ServerSocket(receivePort);
					transientRxSock = true;
				}
				rxSock.setSoTimeout(2000);
				try {
					waiting.add(rxSock.accept());
				} catch (SocketException ex) {
					Alerter.getHandler().severe(
							"Communication Error",
							"A response was expected, but nothing arrived! "
									+ ex.getMessage());
				}
				rxSock.setSoTimeout(0);
			} catch (IOException ex) {
				Alerter.getHandler().severe(
						"Communication Error",
						"Unable to set up receiving connection. "
								+ ex.getMessage());
			}
		}

		// Handle waiting requests, unless there are still none waiting
		if (!waiting.isEmpty()) {
			try (Socket client = waiting.poll();
				ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {

				output = (Message) ois.readObject();
				output.setSource(client.getInetAddress());
			} catch (IOException ex) {
				Alerter.getHandler().severe(
						"Communication Error",
						"Couldn't accept incoming connection. "
								+ ex.getMessage());
			} catch (ClassNotFoundException ex) {
				Alerter.getHandler().severe("Communication Error",
								"Couldn't read incoming message - it appears to be malformed.");
			}
		}
		
		// If the receiving socket is transient, close it off
		if (transientRxSock && rxSock != null) {
			try {
				rxSock.close();
			} catch (IOException ignore) {}
			transientRxSock = false;
		}
		
		return output;
	}

	/**
	 * Add a new communication listener to receive alerts when messages arrive
	 * 
	 * @param listener
	 */
	public static void addCommsListener(CommsListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Remove a communication listener from the registered listeners
	 * 
	 * @param listener
	 */
	public static void removeCommsListener(CommsListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Start an asynchronous message listener
	 */
	public static void listen() {
		listener = new Thread() {
			@Override
			public void run() {
				try {
					while (!isInterrupted()) {
						if (rxSock == null || rxSock.isClosed()) {
							rxSock = new ServerSocket(receivePort);
						}
						try {
							waiting.add(rxSock.accept());
							
							// Handle message reception in a new thread
							new Thread() {
								@Override
								public void run() {
									synchronized (listeners) {
										for (CommsListener l : listeners) {
											l.messageReceived();
										}
									}
								}
							}.start();
						} catch (IOException ex) {
							if (!ex.getMessage().equals("socket closed")) {
								Alerter.getHandler().severe(
										"Connection Listener",
										"Unable to accept incoming message. "
												+ ex.getMessage());
							}
						}
					}
				} catch (IOException ex) {
					Alerter.getHandler().fatal(
							"Connection Listener",
							"Unable to listen for incoming messages. "
									+ ex.getMessage());
				} finally {
					if (rxSock != null) {
						try {
							rxSock.close();
						} catch (IOException ignore) {}
					}
				}
			}
		};
		listener.start();
	}

	/**
	 * Stop the asynchronous message listener
	 */
	public static void hangup() {
		try {
			if (rxSock != null) {
				rxSock.close();
			}
		} catch (IOException ex) {
			Alerter.getHandler().warning("Message Listener", "Unable to correctly hang up - some sockets may not have been closed.");
		}
		listener.interrupt();
	}

	public static InetAddress getRemoteAddr() {
		return remoteAddr;
	}

	public static void setRemoteAddr(InetAddress remoteAddr) {
		Comms.remoteAddr = remoteAddr;
	}

	public static int getReceivePort() {
		return receivePort;
	}

	public static void setReceivePort(int receivePort) {
		Comms.receivePort = receivePort;
	}

	public static int getTransmitPort() {
		return transmitPort;
	}

	public static void setTransmitPort(int transmitPort) {
		Comms.transmitPort = transmitPort;
	}
}
