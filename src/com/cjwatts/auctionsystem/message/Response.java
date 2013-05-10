package com.cjwatts.auctionsystem.message;

public abstract class Response extends Message {

	private static final long serialVersionUID = 1L;

	/*
	 * All responses should renew the session token unless the session has
	 * expired.
	 */
	private byte[] sessionToken;
	private boolean success;
	private String message;

	public byte[] getSessionToken() {
		return sessionToken;
	}

	public void setSessionToken(byte[] sessionToken) {
		this.sessionToken = sessionToken;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
