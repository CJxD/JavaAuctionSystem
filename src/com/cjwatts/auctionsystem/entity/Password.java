package com.cjwatts.auctionsystem.entity;

import java.io.Serializable;

public class Password implements Serializable {
	private static final long serialVersionUID = 1L;

	private byte[] password;
	private byte[] salt;

	public byte[] getPassword() {
		return password;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}

	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}
}
