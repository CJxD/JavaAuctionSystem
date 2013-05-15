package com.cjwatts.auctionsystem.message;

import java.util.Map;

import com.cjwatts.auctionsystem.entity.Item;

public class ViewResponse extends Response {

	private static final long serialVersionUID = 1L;

	private Map<Integer, Item> items;

	public Map<Integer, Item> getItems() {
		return items;
	}

	public void setItems(Map<Integer, Item> items) {
		this.items = items;
	}
}
