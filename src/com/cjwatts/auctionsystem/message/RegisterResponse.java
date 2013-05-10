package com.cjwatts.auctionsystem.message;

public class RegisterResponse extends Response {

	private static final long serialVersionUID = 1L;

	private int newId;

	public int getNewId() {
		return newId;
	}

	public void setNewId(int newId) {
		this.newId = newId;
	}
}
