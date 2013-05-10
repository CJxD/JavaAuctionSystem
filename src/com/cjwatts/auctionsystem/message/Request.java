package com.cjwatts.auctionsystem.message;

import com.cjwatts.auctionsystem.AuctionSystemServer;

public abstract class Request extends Message {

	private static final long serialVersionUID = 1L;

	private String senderId;
	/*
	 * All requests should give a session token to authenticate the user.
	 */
	private byte[] sessionToken;

	public abstract void accept(AuctionSystemServer server);

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public byte[] getSessionToken() {
		return sessionToken;
	}

	public void setSessionToken(byte[] sessionToken) {
		this.sessionToken = sessionToken;
	}
}
