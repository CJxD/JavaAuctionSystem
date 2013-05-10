package com.cjwatts.auctionsystem.io;

public interface CommsListener {
	/**
	 * Indicates that a message has arrived and needs to be retrieved.
	 */
	public void messageReceived();
}
