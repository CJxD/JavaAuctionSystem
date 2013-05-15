package com.cjwatts.auctionsystem.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.cjwatts.auctionsystem.entity.Item;

public class ListingPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public ListingPanel(Item i) {
		super();
		
		ImagePanel picture = new ImagePanel(new BufferedImage(90, 90, BufferedImage.TYPE_INT_ARGB)); // i.getPicture();
		JLabel title = new JLabel(i.getTitle());
		JLabel bid = new JLabel(i.getHighestBid().formatted());
		JLabel time = new DynamicTimeLabel(i);
		
		GroupLayout listingLayout = new GroupLayout(this);
		listingLayout.setHorizontalGroup(listingLayout.createSequentialGroup()
			.addContainerGap()
			.addComponent(picture)
			.addPreferredGap(ComponentPlacement.UNRELATED)
			.addGroup(listingLayout.createParallelGroup()
				.addComponent(title)
				.addComponent(bid)
				.addComponent(time)
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
		
		this.setPreferredSize(new Dimension(200, 90));
		this.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
	}
}
