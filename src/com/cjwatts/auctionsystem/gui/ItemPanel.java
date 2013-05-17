package com.cjwatts.auctionsystem.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.cjwatts.auctionsystem.alert.Alerter;
import com.cjwatts.auctionsystem.entity.Item;
import com.cjwatts.auctionsystem.entity.Item.Bid;

public class ItemPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public ItemPanel(final Item i) {
		super();
		
		ImagePanel picture = new ImagePanel(i.getImage(), 120, 120);
		JLabel title = new JLabel("<html><h3>" + i.getTitle() + "</h3></html>");
		JLabel bid = new JLabel(i.getHighestBid().formattedWithName());
		JLabel history = new JLabel("<html><font color=\"#000099\"><u>View Bid History</u></font></html>");
		history.setCursor(new Cursor(Cursor.HAND_CURSOR));
		JLabel time = new DynamicTimeLabel(i);
		JLabel description = new JLabel("<html><h3>Description</h3>" + i.getDescription() + "</html>");
		
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

				JPanel content = new JPanel();
				content.setLayout(new BorderLayout());
				content.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
				
				JLabel title = new JLabel("<html><h2>Bid History</h2></html>");
				JTextArea text = new JTextArea();
				text.setEditable(false);
				JScrollPane textScroll = new JScrollPane(
						text,
						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				content.add(title, BorderLayout.NORTH);
				content.add(textScroll, BorderLayout.CENTER);
				
				for (Bid b : i.getBids()) {
					text.setText(text.getText() + b.formattedWithName() + '\n');
				}
				
				JDialog dialog = new JDialog(parent, true);
				dialog.setTitle("Bid History");
				dialog.add(content);
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
							.addComponent(newBid, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
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
