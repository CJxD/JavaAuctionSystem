package com.cjwatts.auctionsystem.message;

import com.cjwatts.auctionsystem.AuctionSystemServer;

public class AuthenticateRequest extends Request {

	private static final long serialVersionUID = 1L;

	// Either a password or a session token should be specified
	private byte[] passwordHash;

	@Override
	public void accept(AuctionSystemServer server) {
		server.handle(this);
	}
	
	public byte[] getPassword() {
		return passwordHash;
	}

	public void setPassword(byte[] passwordHash) {
		this.passwordHash = passwordHash;
	}

}
