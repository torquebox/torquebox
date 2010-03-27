/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.torquebox.ruby.enterprise.endpoints;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.jboss.logging.Logger;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.ruby.enterprise.endpoints.cxf.RubyDataBinding;
import org.torquebox.ruby.enterprise.endpoints.cxf.RubyEndpointInvoker;
import org.torquebox.ruby.enterprise.endpoints.cxf.RubyReflectionServiceFactoryBean;
import org.torquebox.ruby.enterprise.endpoints.cxf.RubyServiceConfiguration;
import org.torquebox.ruby.enterprise.endpoints.databinding.RubyTypeSpace;

/**
 * The bean within MC representing a deployed Ruby WebService.
 * 
 * @author Bob McWhirter
 */
public class RubyEndpoint {

	private static final Logger log = Logger.getLogger(RubyEndpoint.class);

	private RubyRuntimePool runtimePool;
	private RubyTypeSpace typeSpace;
	private Bus bus;
	private Server server;

	private String name;
	private URL wsdlLocation;

	private String classLocation;
	private String endpointClassName;

	private String targetNamespace;
	private String portName;

	private String address;

	private boolean verifyTimestamp;
	private boolean verifySignature;

	private String trustStoreFile;
	private String trustStorePassword;

	public RubyEndpoint() {

	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setBus(Bus bus) {
		this.bus = bus;
	}

	public Bus getBus() {
		return this.bus;
	}

	public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
		this.runtimePool = runtimePool;
	}

	public RubyRuntimePool getRubyRuntimePool() {
		return this.runtimePool;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return this.address;
	}

	public void setClassLocation(String classLocation) {
		this.classLocation = classLocation;
	}

	public String getClassLocation() {
		return this.classLocation;
	}

	public void setEndpointClassName(String endpointClassName) {
		this.endpointClassName = endpointClassName;
	}

	public String getEndpointClassName() {
		return this.endpointClassName;
	}

	public void setWsdlLocation(URL wsdlLocation) {
		log.info( "setWsdlLocation [" + wsdlLocation + "]" );
		this.wsdlLocation = wsdlLocation;
	}

	public URL getWsdlLocation() {
		return this.wsdlLocation;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	public String getTargetNamespace() {
		return this.targetNamespace;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getPortName() {
		return this.portName;
	}

	public void setVerifyTimestamp(boolean verifyTimestamp) {
		this.verifyTimestamp = verifyTimestamp;
	}

	public boolean isVerifyTimestamp() {
		return this.verifyTimestamp;
	}

	public void setVerifySignature(boolean verifySignature) {
		this.verifySignature = verifySignature;
	}

	public boolean isVerifySignature() {
		return this.verifySignature;
	}
	
	public void setTrustStoreFile(String trustStoreFile) {
		this.trustStoreFile = trustStoreFile;
	}
	
	public String getTrustStoreFile() {
		return this.trustStoreFile;
	}
	
	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}
	
	public String getTrustStorePassword() {
		return this.trustStorePassword;
	}

	public void setRubyTypeSpace(RubyTypeSpace typeSpace) {
		this.typeSpace = typeSpace;
	}

	public RubyTypeSpace getRubyTypeSpace() {
		return this.typeSpace;
	}

	public void start() {
		log.info( "Starting Ruby Endpoint: " + this.endpointClassName );
		AbstractServiceConfiguration serviceConfig = new RubyServiceConfiguration(getPortName());
		ReflectionServiceFactoryBean serviceFactory = new RubyReflectionServiceFactoryBean();
		serviceFactory.setServiceConfigurations(Collections.singletonList(serviceConfig));

		ServerFactoryBean serverFactory = new ServerFactoryBean();
		serverFactory.setStart(false);
		serverFactory.setBus(bus);
		serverFactory.setServiceFactory(serviceFactory);

		RubyDataBinding dataBinding = new RubyDataBinding(this.runtimePool);
		dataBinding.setRubyTypeSpace(this.typeSpace);

		serviceFactory.setDataBinding(dataBinding);

		RubyEndpointHandler serviceBean = createServiceBean();
		serverFactory.setServiceName(new QName(getTargetNamespace(), getPortName()));
		serverFactory.setEndpointName(new QName(getTargetNamespace(), getPortName() + "Port"));
		serverFactory.setServiceClass(RubyEndpointHandler.class);
		serverFactory.setInvoker(createInvoker(serviceBean));

		serverFactory.setAddress(getAddress());
		serverFactory.setWsdlURL(getWsdlLocation().toExternalForm());

		SoapBindingFactory bindingFactory = new SoapBindingFactory();
		serverFactory.setBindingFactory(bindingFactory);

		this.server = serverFactory.create();

		if (isVerifySignature() || isVerifyTimestamp()) {
			setUpSecurity();
		}

		// this.server.getEndpoint().getInInterceptors().add( new
		// LoggingInInterceptor() );
		// this.server.getEndpoint().getOutFaultInterceptors().add( new
		// LoggingOutInterceptor() );

		this.server.start();
	}

	private void setUpSecurity() {
		WSS4JInInterceptor inSecurityInterceptor = new WSS4JInInterceptor( createSecurityProps() );
		this.server.getEndpoint().getInInterceptors().add(inSecurityInterceptor);
	}
	
	 private Map<String, Object> createSecurityProps() {
		    Map<String, Object> props = new HashMap<String,Object>();
		    String actions = "";
		    if ( isVerifySignature() ) {
		      actions += "Signature ";
		    }
		    if ( isVerifyTimestamp() ) {
		      actions += "Timestamp ";
		    }
		    props.put(WSHandlerConstants.ACTION, actions.trim() );
		    props.put(WSHandlerConstants.SIG_PROP_REF_ID, "jboss.ruby.webservices.crypto.config" );
		    props.put("jboss.ruby.webservices.crypto.config", createCryptoProps() );
		    return props;
		  }
		  
		  private Properties createCryptoProps() {
		    Properties props = new Properties();
		    props.setProperty( "org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin" );
		    props.setProperty( "org.apache.ws.security.crypto.merlin.keystore.type", "jks" );
		    props.setProperty( "org.apache.ws.security.crypto.merlin.keystore.password", getTrustStorePassword() );
		    props.setProperty( "org.apache.ws.security.crypto.merlin.file", getTrustStoreFile() );
		    return props;
		  }

	private RubyEndpointHandler createServiceBean() {
		return new RubyEndpointHandler(this.runtimePool, this.classLocation, this.endpointClassName, this.typeSpace);
	}

	private Invoker createInvoker(RubyEndpointHandler handler) {
		return new RubyEndpointInvoker(handler);
	}

	public void stop() {
		log.info( "Stopping Ruby Endpoint: " + this.endpointClassName );
		this.server.stop();
		this.server = null;
	}
}
