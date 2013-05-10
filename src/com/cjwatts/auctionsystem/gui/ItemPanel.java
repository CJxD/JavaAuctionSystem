package com.cjwatts.auctionsystem.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.cjwatts.auctionsystem.alert.Alerter;
import com.cjwatts.auctionsystem.entity.Item;

public class ItemPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public ItemPanel(final Item i) {
		super();
		
		ImagePanel picture = new ImagePanel(new BufferedImage(200, 125, BufferedImage.TYPE_INT_ARGB)); // i.getPicture();
		JLabel title = new JLabel(i.getTitle());
		JLabel bid = new JLabel(i.getHighestBid().formatted());
		JLabel time = new JLabel(i.getRemainingTime());
		
		final JSpinner newBid = new CurrencySpinner();
		JButton applyBid = new JButton("Bid");
		
		applyBid.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					float bid = (Float) newBid.getValue();
					
					// Notify listeners to this item panel
					ItemPanelListener[] listeners = listenerList.getListeners(ItemPanelListener.class);
					
					BidEvent be = new BidEvent(this, 0);
					be.setBid(bid);
					
					for (ItemPanelListener l : listeners) {
						l.bidSubmitted(be);
					}
				} catch (ClassCastException ex) {
					Alerter.getHandler().warning("New Bid", "The amount specified is not valid");
				}
			}
		});
		
		JLabel description = new JLabel(i.getDescription());
		
		GroupLayout itemLayout = new GroupLayout(this);
		itemLayout.setHorizontalGroup(itemLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(itemLayout.createParallelGroup()
				.addGroup(itemLayout.createSequentialGroup()
					.addComponent(picture)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(itemLayout.createParallelGroup()
						.addComponent(title)
						.addComponent(bid)
						.addComponent(time)
						.addGroup(itemLayout.createSequentialGroup()
							.addComponent(newBid)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(applyBid)
						)
					)
				)
				.addComponent(description)
			)
			.addContainerGap()
		);
		itemLayout.setVerticalGroup(itemLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(itemLayout.createParallelGroup()
				.addComponent(picture)
				.addGroup(itemLayout.createSequentialGroup()
					.addComponent(title)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(bid)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(time)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(itemLayout.createParallelGroup()
						.addComponent(newBid)
						.addComponent(applyBid)
					)
				)
			)
			.addPreferredGap(ComponentPlacement.UNRELATED)
			.addComponent(description)
			.addContainerGap()
		);
		this.setLayout(itemLayout);
	}

	public void addItemPanelListener(ItemPanelListener l) {
		listenerList.add(ItemPanelListener.class, l);
	}
	
	public void removeItemPanelListener(ItemPanelListener l) {
		listenerList.remove(ItemPanelListener.class, l);
	}
}
