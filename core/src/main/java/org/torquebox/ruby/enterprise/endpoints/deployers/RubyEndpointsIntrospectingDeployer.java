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
package org.torquebox.ruby.enterprise.endpoints.deployers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.ruby.core.deployers.AbstractRubyIntrospectingDeployer;
import org.torquebox.ruby.enterprise.endpoints.metadata.RubyEndpointMetaData;
import org.torquebox.ruby.enterprise.endpoints.metadata.RubyEndpointsMetaData;
import org.torquebox.ruby.enterprise.endpoints.metadata.SecurityMetaData;

public class RubyEndpointsIntrospectingDeployer extends AbstractRubyIntrospectingDeployer<RubyEndpointMetaData> {

	public RubyEndpointsIntrospectingDeployer() {
		setInput(RubyEndpointsMetaData.class);
		addOutput(RubyEndpointsMetaData.class);
	}

	public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
		RubyEndpointsMetaData endpoints = unit.getAttachment(RubyEndpointsMetaData.class);

		for (RubyEndpointMetaData endpoint : endpoints.getEndpoints()) {
			introspect(unit, endpoint, endpoint.getEndpointClassName(), endpoint.getClassLocation());
		}
	}

	@Override
	protected void introspect(VFSDeploymentUnit unit, RubyEndpointMetaData metaData, Ruby ruby, RubyModule module) throws DeploymentException {
		if (respondTo(module, "endpoint_configuration")) {
			IRubyObject config = (IRubyObject) reflect(module, "endpoint_configuration");

			if (metaData.getTargetNamespace() == null) {
				if (respondTo(config, "target_namespace")) {
					String targetNamespace = (String) reflect(config, "target_namespace");
					metaData.setTargetNamespace(targetNamespace);
				}
			}

			if (metaData.getPortName() == null) {
				if (respondTo(config, "port_name")) {
					String portName = (String) reflect(config, "port_name");
					metaData.setPortName(portName);
				}
			}

			if (metaData.getSecurityMetaData() == null) {
				if (respondTo(config, "security")) {
					SecurityMetaData securityMetaData = (SecurityMetaData) reflect(config, "security");
					metaData.setSecurityMetaData(securityMetaData);
				}
			}
			
			if ( respondTo(config, "wsdl_location")) {
				String wsdlLocation = (String) reflect( config, "wsdl_location" );
				if ( wsdlLocation != null ) {
					try {
						URL wsdlUrl = new URL( wsdlLocation );
						metaData.setWsdlLocation( wsdlUrl );
					} catch (MalformedURLException e) {
						try {
							VirtualFile wsdlFile = unit.getRoot().getChild( wsdlLocation );
							if ( wsdlFile != null ) {
								metaData.setWsdlLocation( wsdlFile.toURL() );
							}
						} catch (IOException e1) {
							// ignore
						}
					}
				}
			}
		}

	}

}
