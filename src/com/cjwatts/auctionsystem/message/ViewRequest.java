package com.cjwatts.auctionsystem.message;

import java.util.Date;

import com.cjwatts.auctionsystem.AuctionSystemServer;
import com.cjwatts.auctionsystem.entity.Category;

public class ViewRequest extends Request {

	private static final long serialVersionUID = 1L;

	private boolean automatic;
	
	/**
	 * Set this to retrieve items being bid on by a particular user
	 */
	private String bidder = null;
	
	/**
	 * Set this to retrieve items from a particular category
	 */
	private Category category = null;
	
	/**
	 * Set this to retrieve a specific item only
	 */
	private Integer itemId = null;
	
	/**
	 * Set this to retrieve items that started after a given time
	 */
	private Date startTime = null;
	
	/**
	 * Set this to retrieve items from a particular seller
	 */
	private String vendor = null;

	@Override
	public void accept(AuctionSystemServer server) {
		server.handle(this);
	}
	
	/**
	 * Removes all filter criteria from the request
	 */
	public void clearFilters() {
		setBidder(null);
		setCategory(null);
		setItemId(null);
		setStartTime(null);
		setVendor(null);
	}

	public String getBidder() {
		return bidder;
	}

	public Category getCategory() {
		return category;
	}

	public Integer getItemId() {
		return itemId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public String getVendor() {
		return vendor;
	}

	/**
	 * @return True if session token is not to be renewed.
	 */
	public boolean isAutomatic() {
		return automatic;
	}

	/**
	 * @param automatic If set, session token is not renewed
	 * 						and timeouts may still occur.
	 */
	public void setAutomatic(boolean automatic) {
		this.automatic = automatic;
	}

	public void setBidder(String bidder) {
		this.bidder = bidder;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	
}
