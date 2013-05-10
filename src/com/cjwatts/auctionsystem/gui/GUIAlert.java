package com.cjwatts.auctionsystem.gui;

import java.awt.Component;

import javax.swing.JOptionPane;

import com.cjwatts.auctionsystem.alert.AlertHandler;

public class GUIAlert implements AlertHandler {

	private Component parentComponent;

	/**
	 * Generate a pop-up alert handler
	 * 
	 * @param parentComponent
	 *            Determines the Frame in which the dialog is displayed; if
	 *            null, or if the parentComponent has no Frame, a default Frame
	 *            is used
	 */
	public GUIAlert(Component parentComponent) {
		this.parentComponent = parentComponent;
	}

	@Override
	public void info(Object message) {
		info("Information", message);
	}

	@Override
	public void info(String title, Object message) {
		JOptionPane.showMessageDialog(parentComponent, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void warning(Object message) {
		warning("Warning", message);
	}

	@Override
	public void warning(String title, Object message) {
		JOptionPane.showMessageDialog(parentComponent, message, title,
				JOptionPane.WARNING_MESSAGE);
	}

	@Override
	public void severe(Object message) {
		severe("Severe Error", message);
	}

	@Override
	public void severe(String title, Object message) {
		JOptionPane.showMessageDialog(parentComponent, message, title,
				JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void fatal(Object message) {
		fatal("Fatal Error", message);
	}

	@Override
	public void fatal(String title, Object message) {
		message += "\nThe system will now shut down to avoid further issues.";
		JOptionPane.showMessageDialog(parentComponent, message, title,
				JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

}
