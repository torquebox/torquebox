package org.torquebox.ruby.enterprise.client;

import java.net.InetSocketAddress;

public class RubyClient {
	
	public static final String DEFAULT_NAMING_HOST = "localhost";
	public static final int DEFAULT_NAMING_PORT = 1099;
	
	private static final ThreadLocal<RubyClient> threadClient = new ThreadLocal<RubyClient>();
	
	private String applicationName;
	private InetSocketAddress namingAddress;

	protected RubyClient(String applicationName, String namingHost, int namingPort) {
		this.applicationName = applicationName;
		this.namingAddress  = new InetSocketAddress( namingHost, namingPort );
	}
	
	public String getApplicationName() {
		return this.applicationName;
	}
	
	public InetSocketAddress getNamingAddress() {
		return this.namingAddress;
	}
	
	public String getNamingUrl() {
		return "jnp://" + this.namingAddress;
	}
	
	public void close() {
		threadClient.remove();
	}
	
	public static RubyClient connect(String applicationName) {
		return connect( applicationName, DEFAULT_NAMING_HOST, DEFAULT_NAMING_PORT );
	}
	
	public static RubyClient connect(String applicationName, String namingHost, int namingPort) {
		RubyClient client = new RubyClient( applicationName, namingHost, namingPort );
		threadClient.set( client );
		return client;
	}
	
	public static RubyClient getClientForCurrentThread() {
		return threadClient.get();
	}
	

}
