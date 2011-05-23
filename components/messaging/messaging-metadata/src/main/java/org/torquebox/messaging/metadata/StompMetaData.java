package org.torquebox.messaging.metadata;

public class StompMetaData {
	
	public static final int DEFAULT_PORT = 61613;
	
	private int port = DEFAULT_PORT;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
