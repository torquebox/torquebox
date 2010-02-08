package org.torquebox.enterprise.ruby.messaging.container;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ContainerFactory {

	private static final String CONTEXT_FACTORY_CLASS_NAME = "org.jnp.interfaces.NamingContextFactory";
	private static final String URL_PKG_PREFIXES = "org.jboss.naming.client";
	
	private String namingProviderHost = "localhost";
	private int namingProviderPort = 1099;
	private String connectionFactoryJndiName;

	public ContainerFactory() {

	}

	public void setNamingProviderHost(String namingProviderHost) {
		this.namingProviderHost = namingProviderHost;
	}

	public String getNamingProviderHost() {
		return this.namingProviderHost;
	}

	public void setNamingProviderPort(int namingProviderPort) {
		this.namingProviderPort = namingProviderPort;
	}

	public int getNamingProviderPort() {
		return this.namingProviderPort;
	}

	public String getNamingProviderUrl() {
		return "jnp://" + getNamingProviderHost() + ":" + getNamingProviderPort() + "/";
	}

	public InitialContext createInitialContext() throws NamingException {
		Properties environment = new Properties();

		environment.setProperty(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY_CLASS_NAME);
		environment.setProperty(Context.PROVIDER_URL, getNamingProviderUrl());
		environment.setProperty(Context.URL_PKG_PREFIXES, URL_PKG_PREFIXES );

		return new InitialContext(environment);
	}
	
	public void setConnectionFactoryJndiName(String connectionFactoryJndiName) {
		this.connectionFactoryJndiName = connectionFactoryJndiName;
	}
	
	public String getConnectionFactoryJndiName() {
		return this.connectionFactoryJndiName;
	}
	
	public Container createContainer() throws NamingException {
		Container container = new Container();
		container.setContext( createInitialContext() );
		container.setConnectionFactoryJndiName( getConnectionFactoryJndiName() );
		return container;
	}
	

}
