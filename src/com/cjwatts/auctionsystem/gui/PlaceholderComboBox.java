package com.cjwatts.auctionsystem.gui;

import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComboBox;

public class PlaceholderComboBox extends JComboBox<Object> implements FocusListener {
	private static final long serialVersionUID = 1L;

	private String placeholder;
	private Font normalFont;
	private Font placeholderFont;

	public PlaceholderComboBox(String placeholder) {
		super();
		this.addFocusListener(this);
		this.placeholder = placeholder;
		
		this.normalFont = this.getFont();
		this.placeholderFont = new Font(normalFont.getFontName(), Font.ITALIC, normalFont.getSize());
		
		this.focusLost(null);
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (this.getSelectedIndex() == -1) {
			this.setFont(normalFont);
			this.removeItem(placeholder);
			this.insertItemAt(new String(""), 0);
			this.setSelectedIndex(0);
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (this.getSelectedIndex() == -1) {
			this.setFont(placeholderFont);
			this.removeItem("");
			this.insertItemAt(placeholder, 0);
			this.setSelectedItem(placeholder);
		}
	}

	@Deprecated
	public Object getSelectedItem() {
		return super.getSelectedItem();
	}
	
	/**
	 * Replaces the functionality of {@link #getSelectedItem}
	 * by disabling the placeholder object from being returned.
	 */
	public Object getRealSelectedItem() {
		Object item = super.getSelectedItem();
		return isPlaceholder(item) ? null : item;
	}
	
	@Override
	public int getSelectedIndex() {
		int index = super.getSelectedIndex();
		Object item = this.getItemAt(index);
		return isPlaceholder(item) ? -1 : index;
	}
	
	private boolean isPlaceholder(Object item) {
		return (item == null || item.equals(placeholder) || item.equals(""));
	}
}