package com.cjwatts.auctionsystem.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Date;
import java.util.PriorityQueue;

public class Item implements Serializable {

	private static final long serialVersionUID = 1L;

	private String title;
	private String description;
	private String category;
	private int vendor;

	private Timestamp start, end;
	private PriorityQueue<Bid> bids = new PriorityQueue<>();

	public Item() {
		setStart(new Timestamp(new Date().getTime()));
		// Add start bid of 0.00
		bids.add(new Bid("[Reserve]", 0.00f));
	}
	
	/**
	 * @return The highest bid on this item
	 */
	public Bid getHighestBid() {
		return bids.peek();
	}

	/**
	 * Add a bid to this item.
	 * 
	 * @param username
	 *            The username of the bidder
	 * @param bid
	 *            The monetary value of the bid
	 * @return True if bid was successful.
	 */
	public boolean addBid(String username, Float bid) {
		// Add the bid only if it's higher than the maximum and the user is
		// different
		Bid newBid = new Bid(username, bid);
		Bid highBid = getHighestBid();
		if (highBid.compareTo(newBid) < 0 && !highBid.username.equals(username)) {
			bids.add(newBid);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Remove a previous bid from this item.
	 * 
	 * @param userId
	 *            The id of the bidder
	 * @param bid
	 *            The monetary value of the bid
	 */
	public void removeBid(String username, Float bid) {
		Bid b = new Bid(username, bid);
		bids.remove(b);
	}

	/**
	 * Remove all bids of a specific user
	 * 
	 * @return True if bid was successful.
	 */
	public void removeBids(Integer userId) {
		for (Bid b : bids) {
			if (b.username.equals(userId)) {
				bids.remove(b);
			}
		}
	}

	/**
	 * Remove all bids from the item
	 */
	public void clearBids() {
		bids.clear();
	}
	
	/**
	 * @return Time left in seconds
	 */
	public long timeLeft() {
		return (end.getTime() - new Date().getTime()) / 1000;
	}

	/**
	 * @return String formatted in the form Uy Vm Wd Xh Ym Zs
	 */
	public String getRemainingTime() {
		long seconds = timeLeft();
		
		// This is only an approximation - it doesn't count actual calendar months
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		long months = days / 30;
		long years = days / 365;
		
		// Take the modulus of each component to get relative time
		seconds %= 60;
		minutes %= 60;
		hours %= 24;
		days %= 30;
		months %= 12;
		
		StringBuilder time = new StringBuilder();
		if (years > 0) time.append(years + "y ");
		if (months > 0) time.append(months + "m ");
		if (days > 0) time.append(days + "d ");
		if (hours > 0) time.append(hours + "h ");
		time.append(minutes + "m ");
		time.append(seconds + "s");
		
		return time.toString();
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getVendor() {
		return vendor;
	}

	public void setVendor(int vendor) {
		this.vendor = vendor;
	}

	public Timestamp getStart() {
		return start;
	}

	public void setStart(Timestamp start) {
		this.start = start;
	}

	public Timestamp getEnd() {
		return end;
	}

	public void setEnd(Timestamp end) {
		this.end = end;
	}

	public class Bid implements Comparable<Bid> {
		public String username;
		public Float bid;

		public Bid(String username, Float bid) {
			this.username = username;
			this.bid = bid;
		}
		
		/**
		 * @return The bid value formatted as currency
		 */
		public String formatted() {
			NumberFormat formatter = NumberFormat.getCurrencyInstance();
			return formatter.format(bid);
		}

		@Override
		public int compareTo(Bid o) {
			return bid.compareTo(o.bid);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bid == null) ? 0 : bid.hashCode());
			result = prime * result
					+ ((username == null) ? 0 : username.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Bid other = (Bid) obj;
			if (bid == null) {
				if (other.bid != null)
					return false;
			} else if (!bid.equals(other.bid))
				return false;
			if (username == null) {
				if (other.username != null)
					return false;
			} else if (!username.equals(other.username))
				return false;
			return true;
		}
	}
}