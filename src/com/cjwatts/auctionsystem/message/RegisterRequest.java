package com.cjwatts.auctionsystem.message;

import com.cjwatts.auctionsystem.AuctionSystemServer;
import com.cjwatts.auctionsystem.entity.User;

public class RegisterRequest extends Request {
	private static final long serialVersionUID = 1L;

	private User user;
	private byte[] passwordHash;

	@Override
	public void accept(AuctionSystemServer server) {
		server.handle(this);
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public byte[] getPassword() {
		return passwordHash;
	}

	public void setPassword(byte[] passwordHash) {
		this.passwordHash = passwordHash;
	}
}