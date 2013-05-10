package com.cjwatts.auctionsystem.gui;

import java.util.EventListener;

import com.cjwatts.auctionsystem.gui.BidEvent;

public interface ItemPanelListener extends EventListener {
	/**
	 * Invoked when the user places a bid on this item
	 */
	public void bidSubmitted(BidEvent e);
}