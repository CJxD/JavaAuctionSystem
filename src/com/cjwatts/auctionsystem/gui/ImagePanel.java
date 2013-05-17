package com.cjwatts.auctionsystem.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int cornerRadius = 10;
	
	private final BufferedImage image;

	public ImagePanel(BufferedImage image, int width, int height) {
		this.image = image;
		Dimension size = new Dimension(width, height);
		this.setMinimumSize(size);
		this.setSize(size);
		this.setMaximumSize(size);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		int width = this.getWidth();
		int height = this.getHeight();

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		if (this.getBorder() == null) {
			// Add rounded image corners mask if no border is set
			g2.setClip(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));
		}
		
		g2.drawImage(image, 0, 0, width, height, null);
		    
		if (this.getBorder() == null) {
		    // Draw border if no default one set
			g2.setClip(null);
 			g2.setColor(new Color(200, 200, 200));
 			g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
		}
	}
}