package com.cjwatts.auctionsystem.io;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.junit.BeforeClass;
import org.junit.Test;

import com.cjwatts.auctionsystem.alert.Alerter;
import com.cjwatts.auctionsystem.message.AuthenticateRequest;
import com.cjwatts.auctionsystem.message.AuthenticateResponse;
import com.cjwatts.auctionsystem.message.Message;

public class CommsTest {

	@BeforeClass
	public static void setupAlerts() {
		Alerter.setHandler(new AuctionConsole());
		
		Comms.setRemoteAddr(InetAddress.getLoopbackAddress());
		Comms.setReceivePort(7501);
		Comms.setTransmitPort(7500);
		
		serverExists();
	}

	public static void serverExists() {
		boolean reachable = false;
		try (Socket socket = new Socket(Comms.getRemoteAddr(), Comms.getTransmitPort())) {
		    reachable = true;
		} catch (IOException ignore) {}
		assertTrue("There doesn't appear to be a server online.", reachable);
	}
	
	/*
	 * This test may cause the server to panic a little, as it wont accept a response
	 * Don't worry, it's all ok
	 */
	@Test
	public void testSend() {
		AuthenticateRequest testMessage = new AuthenticateRequest();
		testMessage.setSenderId("imashark");
		testMessage.setPassword("philosoraptor".getBytes());
		assertTrue("Couldn't send message.", Comms.sendMessage(testMessage));
	}

	@Test(timeout = 5000)
	public void testResponse() {
		// Requires AuthenticateResponse from server (doesn't need to be valid)
		testSend();
		Message response = Comms.receiveMessage();
		
		assertNotNull("No message received.", response);
		assertTrue("Expected AuthenticateResponse, got " + response.getClass(),
				response instanceof AuthenticateResponse);
	}
}
