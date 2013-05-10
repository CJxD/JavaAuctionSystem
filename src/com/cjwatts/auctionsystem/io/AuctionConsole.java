package com.cjwatts.auctionsystem.io;

import java.io.Console;
import java.util.Date;
import java.util.Scanner;

import com.cjwatts.auctionsystem.alert.AlertHandler;

public class AuctionConsole implements AlertHandler {
	private final Console console = System.console();
	private final Scanner input;

	public AuctionConsole() {
		if (console != null) {
			input = new Scanner(console.reader());
		} else {
			input = null;
		}
	}

	// 00:00:00 [LEVEL] Message
	private static final String consoleFormat = "%1$tT [%2$s] %3$s\n";

	protected void print(String level, Object message) {
		if (console != null) {
			console.printf(consoleFormat, new Date(), level, message);
		} else {
			System.out.printf(consoleFormat, new Date(), level, message);
		}
	}

	@Override
	public void info(Object message) {
		print("INFO", message);
	}

	@Override
	public void info(String title, Object message) {
		String m = "[" + title + "] " + message.toString();
		info(m);
	}

	@Override
	public void warning(Object message) {
		print("WARNING", message);
	}

	@Override
	public void warning(String title, Object message) {
		String m = "[" + title + "] " + message.toString();
		warning(m);
	}

	@Override
	public void severe(Object message) {
		print("SEVERE", message);
	}

	@Override
	public void severe(String title, Object message) {
		String m = "[" + title + "] " + message.toString();
		severe(m);
	}

	@Override
	public void fatal(Object message) {
		print("FATAL", message);
		
	}

	@Override
	public void fatal(String title, Object message) {
		String m = "[" + title + "] " + message.toString();
		m += "\nThe system will now shut down to avoid further issues.";
		fatal(m);
		System.exit(1);
	}
	
	/**
	 * Read input from the console, if any
	 * 
	 * @return A line of user input
	 */
	public String readLine() {
		if (input != null) {
			if (input.hasNext()) {
				return input.next();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
