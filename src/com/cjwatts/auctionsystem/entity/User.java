package com.cjwatts.auctionsystem.entity;

import java.io.Serializable;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	private String firstName;
	private String lastName;

	private transient byte[] sessionToken;
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public byte[] getSessionToken() {
		return sessionToken;
	}

	public void setSessionToken(byte[] sessionToken) {
		this.sessionToken = sessionToken;
	}
}
