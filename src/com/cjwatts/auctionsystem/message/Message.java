package com.cjwatts.auctionsystem.message;

import java.io.Serializable;
import java.net.InetAddress;

public abstract class Message implements Serializable {
	private static final long serialVersionUID = 1L;

	private InetAddress source;

	public InetAddress getSource() {
		return source;
	}

	public void setSource(InetAddress source) {
		this.source = source;
	}
}
