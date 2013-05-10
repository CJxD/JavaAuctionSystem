package com.cjwatts.auctionsystem.alert;

public interface AlertHandler {
	/**
	 * Write an informational message to the user
	 * 
	 * @param message
	 */
	public void info(Object message);

	/**
	 * Write an informational message to the user
	 * 
	 * @param title
	 * @param message
	 */
	public void info(String title, Object message);

	/**
	 * Write a warning message to the user
	 * 
	 * @param message
	 */
	public void warning(Object message);

	/**
	 * Write a warning message to the user
	 * 
	 * @param title
	 * @param message
	 */
	public void warning(String title, Object message);

	/**
	 * Write a severe error to the user
	 * 
	 * @param message
	 */
	public void severe(Object message);

	/**
	 * Write a severe error to the user
	 * 
	 * @param title
	 * @param message
	 */
	public void severe(String title, Object message);
	
	/**
	 * Write a fatal error to the user and close the program
	 * 
	 * @param message
	 */
	public void fatal(Object message);

	/**
	 * Write a fatal error to the user and close the program
	 * 
	 * @param title
	 * @param message
	 */
	public void fatal(String title, Object message);
}
