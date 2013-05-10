package com.cjwatts.auctionsystem.message;

import com.cjwatts.auctionsystem.entity.Item;

public class ViewResponse extends Response {

	private static final long serialVersionUID = 1L;

	private Item item;

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}
}
