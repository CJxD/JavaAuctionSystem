package com.cjwatts.auctionsystem.message;

import com.cjwatts.auctionsystem.AuctionSystemServer;
import com.cjwatts.auctionsystem.entity.Item;

public class CreateItemRequest extends Request {

	private static final long serialVersionUID = 1L;

	private Item item;

	@Override
	public void accept(AuctionSystemServer server) {
		server.handle(this);
	}
	
	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}
}
