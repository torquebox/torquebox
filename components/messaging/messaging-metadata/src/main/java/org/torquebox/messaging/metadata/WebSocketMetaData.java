package org.torquebox.messaging.metadata;

import java.util.Map;

public class WebSocketMetaData {

	public static final String DEFAULT_CONTEXT = "/websockets";
	public static final int DEFAULT_PORT = 40101;

	private String context = DEFAULT_CONTEXT;
	private String handler;
	private int port = DEFAULT_PORT;
	private Map<String, Object> rubyConfig;
	private String rubyRequirePath;

	public String getContext() {
		return context;
	}

	public String getHandler() {
		return handler;
	}

	public int getPort() {
		return port;
	}

	public Map<String, Object> getRubyConfig() {
		return rubyConfig;
	}

	public String getRubyRequirePath() {
		return rubyRequirePath;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setRubyConfig(Map<String, Object> rubyConfig) {
		this.rubyConfig = rubyConfig;
	}

	public void setRubyRequirePath(String rubyRequirePath) {
		this.rubyRequirePath = rubyRequirePath;
	}

}
