package com.cjwatts.auctionsystem.entity;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;

import com.cjwatts.auctionsystem.exception.BidException;

public class Item implements Serializable {

	private static final long serialVersionUID = 1L;

	private Category category = Category.MISC;
	private String description = "";
	private transient BufferedImage image = null; // Not serializable automatically
	private String title = "";
	private String vendor = "";

	private Date start, end;
	private PriorityQueue<Bid> bids = new PriorityQueue<>(20, Collections.reverseOrder());

	public Item() {
		// Default start and end times
		this.setStart(new Date());
		this.setEnd(this.getStart());
		
		bids.add(new Bid("", 0.00));
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		if (image != null) {
			out.writeBoolean(true);
			ImageIO.write(image, "png", out);
		} else {
			out.writeBoolean(false);
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		if (in.readBoolean()) {
			image = ImageIO.read(in);
		}
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
	public void addBid(String username, Double bid) throws BidException {
		if (username.equals(vendor)) {
			throw new BidException("You cannot bid on your own item!");
		}
		
		Bid newBid = new Bid(username, bid);
		Bid highBid = getHighestBid();

		if (newBid.compareTo(highBid) <= 0) {
			throw new BidException("Sorry, you have been out-bid by the current highest bidder.");
		}
		
		bids.add(newBid);
	}

	/**
	 * Remove a previous bid from this item.
	 * 
	 * @param userId
	 *            The id of the bidder
	 * @param bid
	 *            The monetary value of the bid
	 */
	public void removeBid(String username, Double bid) {
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
	 * @return The highest bid on this item
	 */
	public Bid getHighestBid() {
		return bids.peek();
	}
	
	/**
	 * @return True if auction has started but not yet finished
	 */
	public boolean isActive() {
		long now = new Date().getTime();
		return (start.getTime() < now) && (now < end.getTime());
	}
	
	/**
	 * @return Time left in milliseconds
	 */
	public long timeLeft() {
		long now = new Date().getTime();
		long left = end.getTime() - now;
		return left < 0 ? 0 : left;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BufferedImage getImage() {
		if (image == null) {
			try {
				return ImageIO.read(new File("res/default.png"));
			} catch (IOException ex) {
				return null;
			}
		} else {
			return image;
		}
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public ArrayList<Bid> getBids() {
		ArrayList<Bid> history = new ArrayList<>(20);
		for (Bid b : bids) {
			if (b.bid > 0) history.add(b);
		}
		return history;
	}
	
	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public class Bid implements Comparable<Bid>, Serializable {

		private static final long serialVersionUID = 1L;
		
		public String username;
		public Double bid;

		public Bid(String username, Double bid) {
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
		
		/**
		 * @return The bid value formatted as currency with the name of the bidder
		 */
		public String formattedWithName() {
			String formatted = formatted();
			if (!username.equals("")) {
				formatted += " (" + username + ")";
			}
			return formatted;
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