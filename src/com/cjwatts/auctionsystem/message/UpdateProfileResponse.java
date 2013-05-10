package com.cjwatts.auctionsystem.message;

import com.cjwatts.auctionsystem.entity.User;

public class UpdateProfileResponse extends Response {
	private static final long serialVersionUID = 1L;

	private User profile;
	
	public User getProfile() {
		return profile;
	}
	
	public void setProfile(User profile) {
		this.profile = profile;
	}
	
}
