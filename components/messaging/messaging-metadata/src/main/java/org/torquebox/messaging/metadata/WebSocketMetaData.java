package org.torquebox.messaging.metadata;


public class WebSocketMetaData {
	
	public static final String 	DEFAULT_CONTEXT = "/websockets";
	public static final int 	DEFAULT_PORT	= 40101;

	private int port = DEFAULT_PORT;
	private String context = DEFAULT_CONTEXT;
	private String handler;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

}
