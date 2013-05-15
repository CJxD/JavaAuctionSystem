package com.cjwatts.auctionsystem.gui;

import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;

import javax.swing.JPasswordField;

public class PlaceholderPasswordField extends JPasswordField implements FocusListener {
	private static final long serialVersionUID = 1L;

	private String placeholder;
	private char echoChar;
	private Font normalFont;
	private Font placeholderFont;

	public PlaceholderPasswordField(String placeholder) {
		super();
		this.addFocusListener(this);
		this.placeholder = placeholder;
		
		this.echoChar = this.getEchoChar();
		this.normalFont = this.getFont();
		this.placeholderFont = new Font(normalFont.getFontName(), Font.ITALIC, normalFont.getSize());
		
		this.focusLost(null);
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (this.getPassword().length == 0) {
			this.setEchoChar(echoChar);
			this.setFont(normalFont);
			this.setText("");
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (this.getPassword().length == 0) {
			this.setEchoChar((char) 0);
			this.setFont(placeholderFont);
			this.setText(placeholder);
		}
	}

	@Override
	public char[] getPassword() {
		char[] pw = super.getPassword();
		return Arrays.equals(pw, placeholder.toCharArray()) ? new char[0] : pw;
	}
}
