package com.cjwatts.auctionsystem.message;

import com.cjwatts.auctionsystem.AuctionSystemServer;

public class BidRequest extends Request {

	private static final long serialVersionUID = 1L;
	private int itemId;
	private float bid;

	@Override
	public void accept(AuctionSystemServer server) {
		server.handle(this);
	}
	
	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public float getBid() {
		return bid;
	}

	public void setBid(float bid) {
		this.bid = bid;
	}
}