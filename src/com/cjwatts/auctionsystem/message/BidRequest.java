package com.cjwatts.auctionsystem.message;

import com.cjwatts.auctionsystem.AuctionSystemServer;

public class BidRequest extends Request {

	private static final long serialVersionUID = 1L;
	private int itemId;
	private double bid;

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

	public double getBid() {
		return bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}
}