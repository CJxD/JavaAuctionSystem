package com.cjwatts.auctionsystem.gui;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JPanel;

public class SideBar extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private Container listings;
	private Runnable updater;
	
	public SideBar() {
		this.setLayout(new BorderLayout());
	}
	
	/**
	 * Set the control box associated with this side bar
	 * @param c
	 */
	public void setControls(Container c) {
		this.add(c, BorderLayout.NORTH);
	}
	
	/**
	 * Set the listings panel associated with this side bar
	 * @param l
	 */
	public void setListings(Container l) {
		this.listings = l;
		this.add(l, BorderLayout.CENTER);
	}
	
	/**
	 * Remove the listings panel on this side bar so it can be reused
	 */
	public void removeListings() {
		this.remove(listings);
	}
	
	/**
	 * Set the action to execute when this side bar becomes activated
	 * @param updater
	 */
	public void setUpdater(Runnable updater) {
		this.updater = updater;
	}
	
	/**
	 * Run preliminary tasks on this side bar
	 */
	public void updateListings() {
		updater.run();
	}
}
