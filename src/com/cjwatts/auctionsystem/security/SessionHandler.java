package com.cjwatts.auctionsystem.security;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

import com.cjwatts.auctionsystem.alert.Alerter;
import com.cjwatts.auctionsystem.entity.Session;
import com.cjwatts.auctionsystem.io.FileHandler;
import com.cjwatts.auctionsystem.io.PersistenceHandler;

public class SessionHandler {
	private PersistenceHandler storage = new FileHandler();
	public static final long SESSION_TIMEOUT = 900; // 15 minutes

	public SessionHandler() {
	}

	public SessionHandler(PersistenceHandler storage) {
		this.storage = storage;
	}

	/**
	 * Generates a sessionToken for the specific user
	 * 
	 * @param username
	 *            The username of the user requesting the token
	 * @return
	 * 		The sessionToken, or null if token couldn't be generated
	 */
	public byte[] generateToken(String username) {
		SecureRandom random = new SecureRandom();
		byte[] token = new byte[64];
		random.nextBytes(token);
		
		Session session = new Session();
		session.setSessionToken(token);
		// Expiry = now + SESSION_TIMEOUT in millis
		session.setExpires(new Timestamp(new Date().getTime() + SESSION_TIMEOUT * 1000));
		
		try {
			storage.selectDb("sessions");
			storage.writeObject(username, session);
		} catch (IOException ex) {
			Alerter.getHandler().severe("Authetication", "Unable to record session. " + ex.getMessage());
			return null;
		}
		
		return token;
	}

	/**
	 * Check for a valid session token
	 * 
	 * @param username
	 *            The username of the user
	 * @param sessionToken
	 *            The last known session token used
	 * @return True if valid, otherwise false
	 */
	public boolean checkSession(String username, byte[] sessionToken) {
		try {
			storage.selectDb("sessions");
			Session session = (Session) storage.readObject(username);

			// Check token
			boolean valid = Arrays.equals(sessionToken, session.getSessionToken());
			// Check expiry
			valid &= new Date().getTime() < session.getExpires().getTime();
			return valid;
		} catch (IOException ex) {
			Alerter.getHandler().severe("Authetication", "Unable to check valid session. " + ex.getMessage());
			return false;
		}
	}

	public PersistenceHandler getStorage() {
		return storage;
	}

	public void setStorage(PersistenceHandler storage) {
		this.storage = storage;
	}
}
