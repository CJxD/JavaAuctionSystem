package com.cjwatts.auctionsystem.message;

import com.cjwatts.auctionsystem.AuctionSystemServer;
import com.cjwatts.auctionsystem.entity.User;

public class UpdateProfileRequest extends Request {
	private static final long serialVersionUID = 1L;

	private User profile;
	
	public User getProfile() {
		return profile;
	}

	/**
	 * @param profile If left null, profile will not be updated
	 * 					but profile data will be returned.
	 */
	public void setProfile(User profile) {
		this.profile = profile;
	}

	@Override
	public void accept(AuctionSystemServer server) {
		server.handle(this);
	}
}
