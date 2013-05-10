package com.cjwatts.auctionsystem.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int padding = 5;

	private final BufferedImage image;

	public ImagePanel(BufferedImage image) {
		this.image = image;
		this.setSize(
				image.getHeight() + (2 * padding),
				image.getWidth() + (2 * padding));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (this.getBorder() == null) {
			g.setColor(new Color(200, 200, 200));
			g.drawRoundRect(0, 0, this.getHeight(), this.getWidth(), 5, 5);
		}
		g.drawImage(image, padding, padding, null);
	}

}