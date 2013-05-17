package com.cjwatts.auctionsystem.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import com.cjwatts.auctionsystem.alert.Alerter;
import com.cjwatts.auctionsystem.entity.Password;
import com.cjwatts.auctionsystem.io.FileHandler;
import com.cjwatts.auctionsystem.io.PersistenceHandler;

public class PasswordHasher {
	private static final int hashStretchFactor = 30000;
	
	private PersistenceHandler storage = new FileHandler();

	public PasswordHasher() {
	}

	public PasswordHasher(PersistenceHandler storage) {
		this.storage = storage;
	}

	/**
	 * Calculates a hash to be sent with an authentication request
	 * 
	 * @param userId
	 *            The id of the user being authenticated
	 * @param password
	 *            Plaintext password
	 */
	public byte[] preHash(String username, String password) {
		try {
			// Hash the password with the user id (a constant salt)
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			String seed = username.toLowerCase() + password;
			byte[] bytes = seed.getBytes("UTF-8");
			
			// Do some key stretching to slow down brute force attacks
			for (int i = 0; i < hashStretchFactor; i++) {
				md.update(bytes);
				bytes = md.digest();
			}
			return bytes;
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
			Alerter.getHandler().severe("Authetication", "Unable to securely prepare password for transmission. Login failed. " + ex.getMessage());
			return null;
		}
	}

	/**
	 * Writes a password hash to storage. Rehashes with a seed before storing.
	 * 
	 * @param username
	 *            The username of the user
	 * @param passwordHash
	 *            The prehashed password
	 */
	public void writePassword(String username, byte[] passwordHash) {
		// Generate salt
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[16];
		random.nextBytes(salt);

		// Hash the password and salt together
		// hash + salt = password
		byte[] p = rehash(passwordHash, salt);

		// Create a new password object
		Password password = new Password();
		password.setPassword(p);
		password.setSalt(salt);
		
		try {
			storage.selectDb("passwords");
			storage.writeObject(username.toLowerCase(), password);
		} catch (IOException ex) {
			Alerter.getHandler().severe("Authetication", "Unable to write password to persistence. " + ex.getMessage());
		}
	}

	/**
	 * Check the password hash in the database against the given password hash.
	 * 
	 * @param username
	 *            The username of the user to be checked
	 * @param passwordHash
	 *            The prehashed password
	 * @return True if valid, otherwise false
	 */
	public boolean checkPassword(String username, byte[] passwordHash) {
		try {
			// hash + salt = password
			storage.selectDb("passwords");
			Object obj = storage.readObject(username.toLowerCase());
			Password password = (Password) obj;
			if (password == null) {
				// User doesn't exist
				return false;
			}
			byte[] p = rehash(passwordHash, password.getSalt());

			return Arrays.equals(p, password.getPassword());
		} catch (IOException ex) {
			Alerter.getHandler().severe("Authetication", "Unable to check password. " + ex.getMessage());
			return false;
		}
	}

	/**
	 * Hashes passwordHash and salt together
	 * 
	 * @param passwordHash
	 * @param salt
	 * @return
	 */
	private byte[] rehash(byte[] passwordHash, byte[] salt) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] bytes = new byte[passwordHash.length + salt.length];

			// Join passwordHash and salt together into a single array, seed
			System.arraycopy(passwordHash, 0, bytes, 0, passwordHash.length);
			System.arraycopy(salt, 0, bytes, passwordHash.length, salt.length);

			// Do some key stretching
			for (int i = 0; i < hashStretchFactor; i++) {
				md.update(bytes);
				bytes = md.digest();
			}
			return bytes;
		} catch (NoSuchAlgorithmException ex) {
			Alerter.getHandler().severe("Authetication", "Unable to re-hash password for saving. " + ex.getMessage());
			return null;
		}
	}

	public PersistenceHandler getStorage() {
		return storage;
	}

	public void setStorage(PersistenceHandler storage) {
		this.storage = storage;
	}
}
