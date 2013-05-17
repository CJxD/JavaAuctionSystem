package com.cjwatts.auctionsystem.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.cjwatts.auctionsystem.entity.Item;
import com.cjwatts.auctionsystem.entity.Item.Bid;

public class ListingPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * @param user The user viewing the item (for won/lost messages)
	 * @param i The item to display
	 */
	public ListingPanel(String user, Item i) {
		super();
		
		ImagePanel picture = new ImagePanel(i.getImage(), 65, 65);
		JLabel title = new JLabel(i.getTitle());
		JLabel bid = new JLabel(i.getHighestBid().formatted());
		JLabel time = new DynamicTimeLabel(i);
		
		// Won/Lost messages
		if (i.timeLeft() == 0) {
			time = new JLabel("Ended.");
			
			boolean isBidder = false;
			Iterator<Bid> it = i.getBids().iterator();
			while (!isBidder && it.hasNext()) {
				isBidder = it.next().username.equals(user);
			}

			if (isBidder) {
				if (i.getHighestBid().username.equals(user)) {
					time = new JLabel("Won!");
					time.setForeground(new Color(0, 100, 0));
				} else {
					time = new JLabel("Lost.");
					time.setForeground(new Color(100, 0, 0));
				}
			}
		}
		
		GroupLayout listingLayout = new GroupLayout(this);
		listingLayout.setHorizontalGroup(listingLayout.createSequentialGroup()
			.addContainerGap()
			.addComponent(picture)
			.addPreferredGap(ComponentPlacement.UNRELATED)
			.addGroup(listingLayout.createParallelGroup()
				.addComponent(title, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(bid, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(time, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			)
			.addContainerGap()
		);
		listingLayout.setVerticalGroup(listingLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(listingLayout.createParallelGroup()
				.addComponent(picture)
				.addGroup(listingLayout.createSequentialGroup()
					.addComponent(title)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(bid)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(time)
				)
			)
			.addContainerGap()
		);
		this.setLayout(listingLayout);
		
		this.setPreferredSize(new Dimension(250, 90));
		this.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
	}
}
