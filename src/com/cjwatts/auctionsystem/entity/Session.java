package com.cjwatts.auctionsystem.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class Session implements Serializable {
	private static final long serialVersionUID = 1L;

	private byte[] sessionToken;
	private Timestamp expires;

	public byte[] getSessionToken() {
		return sessionToken;
	}

	public void setSessionToken(byte[] sessionToken) {
		this.sessionToken = sessionToken;
	}

	public Timestamp getExpires() {
		return expires;
	}

	public void setExpires(Timestamp expires) {
		this.expires = expires;
	}
}
