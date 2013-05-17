package com.cjwatts.auctionsystem;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.cjwatts.auctionsystem.alert.Alerter;
import com.cjwatts.auctionsystem.entity.Category;
import com.cjwatts.auctionsystem.entity.Item;
import com.cjwatts.auctionsystem.entity.Item.Bid;
import com.cjwatts.auctionsystem.entity.User;
import com.cjwatts.auctionsystem.exception.BidException;
import com.cjwatts.auctionsystem.io.AuctionConsole;
import com.cjwatts.auctionsystem.io.Comms;
import com.cjwatts.auctionsystem.io.CommsListener;
import com.cjwatts.auctionsystem.io.FileHandler;
import com.cjwatts.auctionsystem.io.PersistenceHandler;
import com.cjwatts.auctionsystem.message.*;
import com.cjwatts.auctionsystem.security.PasswordHasher;
import com.cjwatts.auctionsystem.security.SessionHandler;

public class AuctionSystemServer implements CommsListener {

	private static PersistenceHandler storage = new FileHandler();

	public static void main(String[] args) {
		AuctionSystemServer server = new AuctionSystemServer();
		AuctionConsole console = new AuctionConsole();
		Alerter.setHandler(console);

		server.init();
		String input;
		// Read input and repeat until "stop" is entered.
		do {
			input = console.readLine();
		} while (input == null || !input.equals("stop"));
		server.close();
	}

	/**
	 * Initialises the server with GUI
	 */
	public void init() {
		// Start GUI
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame("Auction System Server");
				JPanel content = new JPanel();
				final JButton generateReport = new JButton("Generate Report");
				final JButton start = new JButton("Start Server");
				start.setEnabled(false);
				final JButton stop = new JButton("Stop Server");
				final JTextArea report = new JTextArea();
				JScrollPane reportScroll = new JScrollPane(
											report,
											JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
											JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				report.setEditable(false);
				
				generateReport.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						report.setText("Ended auctions:\n");
						try {
							storage.selectDb("items");
							Set<Object> keys = storage.keySet();
							String format = "%s\nItem #%d - '%s':\nWon by %s %s for %s on %s\n";
							Item item;
							String bidderId;
							User bidderProfile;
							for (Object key : keys) {
								item = (Item) storage.readObject(key);
								if (item.getHighestBid() != null) {
									if (item.timeLeft() == 0) {
										bidderId = item.getHighestBid().username;
										storage.selectDb("users");
										bidderProfile = (User) storage.readObject(bidderId);
										
										if (bidderProfile == null) {
											bidderProfile = new User();
											bidderProfile.setFirstName("anonymous");
											bidderProfile.setLastName("user");
										}
										report.setText(
												String.format(
														format,
														report.getText(), 
														key,
														item.getTitle(),
														bidderProfile.getFirstName(),
														bidderProfile.getLastName(),
														item.getHighestBid().formatted(),
														item.getEnd().toString()));
									}
								}
								storage.selectDb("items");
							}
						} catch (IOException ex) {
							Alerter.getHandler().severe("Persistence Error",
									"Unable to retrieve item information.");
						}
					}
				});
				
				start.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						start.setEnabled(false);
						setup();
						stop.setEnabled(true);
					}
				});
				
				stop.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						stop.setEnabled(false);
						close();
						start.setEnabled(true);
					}
				});
				
				// Layout code
				GroupLayout contentLayout = new GroupLayout(content);
				contentLayout.setHorizontalGroup(contentLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(contentLayout.createParallelGroup()
						.addComponent(reportScroll)
						.addComponent(generateReport, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGroup(contentLayout.createSequentialGroup()
							.addComponent(start, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(stop, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						)
					)
					.addContainerGap()
				);
				contentLayout.setVerticalGroup(contentLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(reportScroll)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(generateReport)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(contentLayout.createParallelGroup()
						.addComponent(start)
						.addComponent(stop)
					)
					.addContainerGap()
				);
				content.setLayout(contentLayout);
				
				frame.setContentPane(content);
				frame.pack();
				frame.setSize(new Dimension(480, 500));
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setVisible(true);
			}
		});
		
		setup();
	}
	
	/**
	 * Initialises the actual server
	 */
	public void setup() {
		Alerter.getHandler().info("Auction System server is starting.");

		// Switch the transmit and receive ports over
		int temp = Comms.getReceivePort();
		Comms.setReceivePort(Comms.getTransmitPort());
		Comms.setTransmitPort(temp);
		// Listen for connections
		Comms.addCommsListener(this);
		Comms.listen();
		
		Alerter.getHandler().info("Auction System server is ready.");
	}

	/**
	 * Safely disconnects the server and closes the program
	 */
	public void close() {
		Alerter.getHandler().info("Auction System is shutting down.");
		Comms.hangup();
	}

	@Override
	public void messageReceived() {
		Message received = Comms.receiveMessage();
		if (received != null) {
			try {
				Request request = (Request) received;
				Alerter.getHandler().info(
						"Received " + request.getClass().getSimpleName() + " from "
								+ request.getSource().getHostAddress());
	
				// Use visitor pattern to handle the request appropriately
				request.accept(this);
			} catch (ClassCastException ex) {
				Alerter.getHandler().warning(
						"Could not receive " + received.getClass().getSimpleName()
								+ " from " + received.getSource().getHostAddress()
								+ ": not a valid request.");
			}
		}
	}

	/**
	 * Finish off a response message and send it back
	 * 
	 * @param rq
	 * @param re
	 */
	private void respond(Request rq, Response re) {
		// If no new session token has been provided, generate one
		if (re.getSessionToken() == null) {
			SessionHandler sh = new SessionHandler(storage);
			re.setSessionToken(sh.generateToken(rq.getSenderId()));
		}
		
		String info = re.getClass().getSimpleName() + " to " + rq.getSource().getHostAddress();
		// Send the response to whoever sent the request
		if (Comms.sendMessage(re, rq.getSource())) {
			Alerter.getHandler().info("Sent " + info);
		} else {
			Alerter.getHandler().warning("Failed to send " + info);
		}
	}
	
	/**
	 * Checks if the session token is valid.
	 * If not, the response is returned immediately with error.
	 * 
	 * @param rq
	 * @param re
	 * @return False if session expired
	 */
	private boolean handleSession(Request rq, Response re) {
		SessionHandler sh = new SessionHandler(storage);
		boolean valid = sh.checkSession(rq.getSenderId(), rq.getSessionToken());
		
		if (valid) {
			return true;
		} else {
			re.setSessionToken(new byte[0]);
			re.setMessage("Your session has expired. Please log in again to resume.");
			re.setSuccess(false);
			respond(rq, re);
			return false;
		}
	}
	
	/**
	 * Receives and handles an AuthenticateRequest, returning an
	 * AuthenticateResponse to the sender.
	 * 
	 * @param rq
	 */
	public void handle(AuthenticateRequest rq) {
		// Initiate a new response - action not successful until otherwise
		// specified
		AuthenticateResponse re = new AuthenticateResponse();
		re.setSuccess(false);

		SessionHandler sh = new SessionHandler(storage);
		String id = rq.getSenderId();

		// Either a password or session token can be specified
		if (rq.getPassword() != null) {
			PasswordHasher ph = new PasswordHasher(storage);
			re.setSuccess(ph.checkPassword(id, rq.getPassword()));
			if (!re.isSuccess()) {
				re.setMessage("Login failed. Try again.");
			}
		} else if (rq.getSessionToken() != null) {
			// Automatically handle and respond to invalid sessions
			if (!handleSession(rq, re)) return;
		}

		// Generate a new session token only if successful
		if (re.isSuccess()) {
			re.setSessionToken(sh.generateToken(id));
		} else {
			// This keeps the respond method from generating a token itself
			re.setSessionToken(new byte[0]);
		}

		respond(rq, re);
	}

	/**
	 * Receives and handles a BidRequest, returning a BidResponse to the sender.
	 * 
	 * @param rq
	 */
	public void handle(BidRequest rq) {
		BidResponse re = new BidResponse();
		re.setSuccess(false);
		
		// Automatically handle and respond to invalid sessions
		if (!handleSession(rq, re)) return;

		Item item = null;
		try {
			// Get the item being referenced
			storage.selectDb("items");
			item = (Item) storage.readObject(rq.getItemId());
		} catch (IOException ex) {
			Alerter.getHandler().severe("Persistence Error",
					"Unable to retrieve item information for new bid.");
			re.setMessage("A server error occurred. Please try again later.");
		}

		if (item != null) {
			try {
				// Set the bid and store
				item.addBid(rq.getSenderId(), rq.getBid());
				storage.writeObject(rq.getItemId(), item);
				re.setSuccess(true);
			} catch (BidException ex) {
				re.setMessage(ex.getMessage());
			} catch (IOException ex) {
				Alerter.getHandler().severe("Persistence Error",
						"Unable to save item information for new bid.");
				re.setMessage("A server error occurred. Please try again later.");
			}
		}

		respond(rq, re);
	}

	/**
	 * Receives and handles a CreateItemRequest, returning a CreateItemResponse
	 * to the sender.
	 * 
	 * @param rq
	 */
	public void handle(CreateItemRequest rq) {
		CreateItemResponse re = new CreateItemResponse();
		re.setSuccess(false);
		
		if (!handleSession(rq, re)) return;

		try {
			storage.selectDb("items");
			re.setNewId(storage.writeObject(rq.getItem()));
			re.setSuccess(true);
		} catch (IOException ex) {
			Alerter.getHandler().severe("Persistence Error",
					"Unable to save item information for new bid.");
			re.setMessage("A server error occurred. Please try again later.");
		}

		respond(rq, re);
	}

	/**
	 * Receives and handles a RegisterRequest, returning a RegisterResponse to
	 * the sender.
	 * 
	 * @param rq
	 */
	public void handle(RegisterRequest rq) {
		RegisterResponse re = new RegisterResponse();
		re.setSuccess(false);

		try {
			storage.selectDb("users");
			String id = rq.getSenderId();
			User user = rq.getUser();
			
			// Check if username is already taken
			User check = (User) storage.readObject(id.toLowerCase());
			
			if (check != null) {
				re.setMessage("Username already taken, please try another.");
				re.setSuccess(false);
			} else {
				storage.writeObject(id.toLowerCase(), user);

				PasswordHasher ph = new PasswordHasher(storage);
				ph.writePassword(id, rq.getPassword());
				
				re.setSuccess(true);
			}
		} catch (IOException ex) {
			Alerter.getHandler().severe("Persistence Error",
					"Unable to save new user.");
			re.setMessage("A server error occurred. Please try again later.");
		}

		respond(rq, re);
	}

	/**
	 * Receives and handles an UpdateProfileRequest, returning an UpdateProfileResponse to the
	 * sender.
	 * 
	 * @param rq
	 */
	public void handle(UpdateProfileRequest rq) {
		UpdateProfileResponse re = new UpdateProfileResponse();
		re.setSuccess(false);

		if (!handleSession(rq, re)) return;
		
		try {
			storage.selectDb("users");
			
			User profile = rq.getProfile();
			if (profile != null) {
				// If the profile isn't null, update
				storage.writeObject(rq.getSenderId().toLowerCase(), profile);
			} else {
				// Otherwise, return the existing one
				profile = (User) storage.readObject(rq.getSenderId().toLowerCase());
			}
			re.setProfile(profile);
			re.setSuccess(true);
		} catch (IOException ex) {
			Alerter.getHandler().severe("Persistence Error",
					"Unable to retrieve item information.");
			re.setMessage("A server error occurred. Please try again later.");
		}

		respond(rq, re);
	}
	
	/**
	 * Receives and handles a ViewRequest, returning a ViewResponse to the
	 * sender.
	 * 
	 * @param rq
	 */
	public void handle(ViewRequest rq) {
		ViewResponse re = new ViewResponse();
		re.setSuccess(false);

		if (!handleSession(rq, re)) return;
		
		try {
			storage.selectDb("items");
			
			Map<Integer, Item> fetched = new HashMap<>();
			Set<Object> needed = storage.keySet();
			// TODO: Temporary
			if (needed.isEmpty()) {
				Item test1 = new Item();
				test1.setCategory(Category.COMPUTING);
				test1.setDescription("A new, lightweight, Java auction system that uses sockets to communicate with a central sever.");
				test1.setStart(new Date());
				test1.setEnd(new Date(((long) Integer.MAX_VALUE) * 1000));
				test1.setTitle("Java Auction System");
				test1.setVendor("cw17g12");
				storage.writeObject(test1);
				
				Item test2 = new Item();
				test2.setCategory(Category.MISC);
				test2.setDescription("Contrary to the system in use, this item can only be purchased through a high mark given for the coursework. (16/17.5 or greater)");
				test2.setStart(new Date());
				test2.setEnd(new Date(1370840400L * 1000));
				test2.setTitle("Eternal Respect");
				test2.setVendor("cw17g12");
				storage.writeObject(test2);
				
				Item test3 = new Item();
				test3.setCategory(Category.MISC);
				test3.setDescription("This auction will end within the next hour! Hurry!");
				test3.setStart(new Date());
				test3.setEnd(new Date(test3.getStart().getTime() + 3600000));
				test3.setTitle("Lack of Time");
				test3.setVendor("cw17g12");
				storage.writeObject(test3);
			}
			
			// Fetch the required items
			Item next;
			boolean valid;
			for (Object i : needed) {
				next = (Item) storage.readObject(i);
				
				// Check if auction is running
				if (next.isActive()) {
					valid = true;
				} else {
					valid = false;
				}
				
				/*
				 * Search filters
				 */
				if (rq.getStartTime() != null) {
					if (next.getStart().compareTo(rq.getStartTime()) > 0) {
						// Override even if auction is not running
						valid = true;
					} else {
						valid = false;
					}
				}
				
				if (valid && rq.getItemId() != null) {
					if (!i.equals(rq.getItemId())) {
						valid = false;
					}
				}

				if (valid && rq.getBidder() != null) {
					boolean found = false;
					Iterator<Bid> it = next.getBids().iterator();
					while (!found && it.hasNext()) {
						found = it.next().username.equals(rq.getBidder());
					}
					valid = found;
				}
				
				if (valid && rq.getCategory() != null) {
					if (!next.getCategory().equals(rq.getCategory())) {
						valid = false;
					}
				}
				
				if (valid && rq.getVendor() != null) {
					if (!next.getVendor().equals(rq.getVendor())) {
						valid = false;
					}
				}
				
				// Add this item if it passed the search criteria
				if (valid) {
					fetched.put((Integer) i, next);
				}
			}
			
			re.setItems(fetched);
			re.setSuccess(true);
		} catch (IOException ex) {
			Alerter.getHandler().severe("Persistence Error",
					"Unable to retrieve item information.");
			re.setMessage("A server error occurred. Please try again later.");
		}

		if (rq.isAutomatic()) {
			// Repeat session token
			re.setSessionToken(rq.getSessionToken());
		}
		respond(rq, re);
	}
}
