package com.cjwatts.auctionsystem.alert;

public class Alerter {
	private static AlertHandler handler;

	public static AlertHandler getHandler() {
		return handler;
	}

	public static void setHandler(AlertHandler handler) {
		Alerter.handler = handler;
	}
}
