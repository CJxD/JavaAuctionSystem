package com.cjwatts.auctionsystem;

import java.io.IOException;

import com.cjwatts.auctionsystem.alert.Alerter;
import com.cjwatts.auctionsystem.entity.Item;
import com.cjwatts.auctionsystem.entity.User;
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

		server.setup();
		String input;
		// Read input and repeat until "stop" is entered.
		do {
			input = console.readLine();
		} while (input == null || !input.equals("stop"));
		server.close();
	}

	public void setup() {
		Alerter.getHandler().info("Auction System server is starting.");

		// Switch the transmit and receive ports over
		int temp = Comms.getReceivePort();
		Comms.setReceivePort(Comms.getTransmitPort());
		Comms.setTransmitPort(temp);
		Comms.addCommsListener(this);

		Comms.listen();
		Alerter.getHandler().info("Auction System server is ready.");
	}

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
								+ request.getSource());
	
				// Use visitor pattern to handle the request appropriately
				request.accept(this);
			} catch (ClassCastException ex) {
				Alerter.getHandler().warning(
						"Could not receive " + received.getClass().getSimpleName()
								+ " from " + received.getSource()
								+ ": not a valid request.");
			}
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
			re.setMessage("Session has expired. Please log in again to resume.");
			re.setSuccess(false);
			respond(rq, re);
			return false;
		}
	}
	
	/**
	 * Finish off a response message and send it back
	 * 
	 * @param rq
	 * @param re
	 */
	private void respond(Request rq, Response re) {
		// Send the response to whoever sent the request
		Comms.setRemoteAddr(rq.getSource());
		
		// If no new session token has been provided, generate one
		SessionHandler sh = new SessionHandler(storage);
		re.setSessionToken(sh.generateToken(rq.getSenderId().toLowerCase()));
		
		String info = re.getClass().getSimpleName() + " to " + rq.getSource();
		if (Comms.sendMessage(re)) {
			Alerter.getHandler().info("Sent " + info);
		} else {
			Alerter.getHandler().warning("Failed to send " + info);
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
		String id = rq.getSenderId().toLowerCase();

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
				re.setSuccess(item.addBid(rq.getSenderId(), rq.getBid()));
				storage.writeObject(rq.getItemId(), item);
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
			int id = storage.nextId();
			storage.writeObject(id, rq.getItem());
			re.setNewId(id);
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
			String id = rq.getSenderId().toLowerCase();
			User user = rq.getUser();
			
			// Check if username is already taken
			User check = (User) storage.readObject(id);
			
			if (check != null) {
				re.setMessage("Username already taken, please try another.");
				re.setSuccess(false);
			} else {
				storage.writeObject(id, user);

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
				storage.writeObject(rq.getSenderId(), profile);
			} else {
				// Otherwise, return the existing one
				profile = (User) storage.readObject(rq.getSenderId());
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
			Item item = (Item) storage.readObject(rq.getItemId());
			re.setItem(item);
			re.setSuccess(true);
		} catch (IOException ex) {
			Alerter.getHandler().severe("Persistence Error",
					"Unable to retrieve item information.");
			re.setMessage("A server error occurred. Please try again later.");
		}

		respond(rq, re);
	}
}
