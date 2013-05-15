package com.cjwatts.auctionsystem.gui;

import java.awt.AWTEvent;

public class BidEvent extends AWTEvent {
	private static final long serialVersionUID = 1L;

	private double bid;
	
	public BidEvent(Object source, int id) {
		super(source, id);
	}

	public double getBid() {
		return bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}
}