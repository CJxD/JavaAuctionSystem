package com.cjwatts.auctionsystem.gui;

import java.util.Date;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

public class DateSpinner extends JSpinner {
	private static final long serialVersionUID = 1L;
	
	public DateSpinner() {
		super(new SpinnerDateModel());
		
		this.setEditor(new JSpinner.DateEditor(this, "yyyy-MM-dd HH:mm:ss"));
	}
	
	@Override
	public Date getValue() {
		return (Date) super.getValue();
	}
}
