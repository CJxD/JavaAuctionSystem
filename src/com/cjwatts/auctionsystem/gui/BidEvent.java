package com.cjwatts.auctionsystem.gui;

import java.awt.AWTEvent;

public class BidEvent extends AWTEvent {
	private static final long serialVersionUID = 1L;

	private float bid;
	
	public BidEvent(Object source, int id) {
		super(source, id);
	}

	public float getBid() {
		return bid;
	}

	public void setBid(float bid) {
		this.bid = bid;
	}
}