package com.cjwatts.auctionsystem.gui;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;

import com.cjwatts.auctionsystem.entity.Item;

/**
 * Updates the remaining time every second
 */
public class DynamicTimeLabel extends JLabel implements Runnable {
	private static final long serialVersionUID = 1L;

	private final ScheduledExecutorService timeChanger;
	private Item item;
	
	public DynamicTimeLabel(Item item) {
		this.item = item;
		// Update every second
		timeChanger = Executors.newSingleThreadScheduledExecutor();
		timeChanger.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
	}
	
	@Override
	public void run() {
		this.setText(getRemainingTime(item.timeLeft() / 1000));
	}
	
	/**
	 * @return String formatted in the form Uy Vm Wd Xh Ym Zs
	 */
	private String getRemainingTime(long seconds) {
		// This is only an approximation - it doesn't count actual calendar months
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		long months = days / 30;
		long years = days / 365;
		
		boolean oneDayLeft = days == 0;
		boolean oneHourLeft = hours == 0;
		
		// Take the modulus of each component to get relative time
		seconds %= 60;
		minutes %= 60;
		hours %= 24;
		days %= 30;
		months %= 12;
		
		StringBuilder time = new StringBuilder();
		if (years > 0) time.append(years + "y ");
		if (months > 0) time.append(months + "m ");
		if (days > 0) time.append(days + "d ");
		if (hours > 0) time.append(hours + "h ");
		if (oneDayLeft) time.append(minutes + "m ");
		if (oneHourLeft) time.append(seconds + "s");
		
		return time.toString();
	}
}
