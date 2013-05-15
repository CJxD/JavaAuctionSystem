package com.cjwatts.auctionsystem.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.cjwatts.auctionsystem.alert.Alerter;
import com.cjwatts.auctionsystem.entity.Item;
import com.cjwatts.auctionsystem.entity.Item.Bid;

public class ItemPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public ItemPanel(final Item i) {
		super();
		
		ImagePanel picture = new ImagePanel(new BufferedImage(200, 125, BufferedImage.TYPE_INT_ARGB)); // i.getPicture();
		JLabel title = new JLabel(i.getTitle());
		JLabel bid = new JLabel(i.getHighestBid().formattedWithName());
		JLabel history = new JLabel("<html><font color=\"#000099\"><u>View Bid History</u></font></html>");
		JLabel time = new DynamicTimeLabel(i);
		JLabel description = new JLabel("<html>" + i.getDescription() + "</html>");
		
		final CurrencySpinner newBid = new CurrencySpinner();
		newBid.setValue(0.0 + i.getHighestBid().bid);
		JButton applyBid = new JButton("Bid");
		
		// Apply bid
		applyBid.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Double bid = newBid.getDouble();
					
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
		
		// Bid history
		history.addMouseListener(new MouseAdapter() {
			@Override
		    public void mouseClicked(MouseEvent arg0) {
				JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(ItemPanel.this);

				JLabel text = new JLabel();
				text.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
				text.setText("<html><h2>Bid History</h2>");
				
				for (Bid b : i.getBids()) {
					text.setText(text.getText() + b.formattedWithName() + "<br>");
				}
				text.setText(text.getText() + "</html>");
				
				JDialog dialog = new JDialog(parent, true);
				dialog.setLayout(new BorderLayout());
				dialog.add(text, BorderLayout.NORTH);
				dialog.setSize(new Dimension(200, 300));
				dialog.setLocationRelativeTo(parent);
				dialog.setVisible(true);
		    }
		});
		
		// Layout
		GroupLayout itemLayout = new GroupLayout(this);
		itemLayout.setHorizontalGroup(itemLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(itemLayout.createParallelGroup()
				.addGroup(itemLayout.createSequentialGroup()
					.addComponent(picture)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(itemLayout.createParallelGroup()
						.addComponent(title)
						.addGroup(itemLayout.createSequentialGroup()
								.addComponent(bid)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(history)
						)
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
					.addGroup(itemLayout.createParallelGroup()
							.addComponent(bid)
							.addComponent(history)
					)
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
		this.setBackground(Color.WHITE);
	}

	public void addItemPanelListener(ItemPanelListener l) {
		listenerList.add(ItemPanelListener.class, l);
	}
	
	public void removeItemPanelListener(ItemPanelListener l) {
		listenerList.remove(ItemPanelListener.class, l);
	}
}
