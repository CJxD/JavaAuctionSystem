package com.cjwatts.auctionsystem.message;

import java.sql.Timestamp;

import com.cjwatts.auctionsystem.AuctionSystemServer;
import com.cjwatts.auctionsystem.entity.Category;

public class ViewRequest extends Request {

	private static final long serialVersionUID = 1L;

	/**
	 * Set this to retrieve items from a particular seller
	 */
	private String vendor = null;
	
	/**
	 * Set this to retrieve items from a particular category
	 */
	private Category category = null;
	
	/**
	 * Set this to retrieve items that started after a given time
	 */
	private Timestamp startTime = null;
	
	/**
	 * Set this to retrieve a specific item only
	 */
	private Integer itemId = null;
	
	private boolean automatic;

	@Override
	public void accept(AuctionSystemServer server) {
		server.handle(this);
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
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
	
}
