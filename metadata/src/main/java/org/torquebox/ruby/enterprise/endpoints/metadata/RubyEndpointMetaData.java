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
package org.torquebox.ruby.enterprise.endpoints.metadata;

import java.net.URL;


public class RubyEndpointMetaData {
	
	private String name;
	
	private URL wsdlLocation;
	private String classLocation;
	private String endpointClassName;
	
	private String portName;
	private String targetNamespace;
	
	private SecurityMetaData securityMetaData;

	public RubyEndpointMetaData() {
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setWsdlLocation(URL wsdlLocation) {
		this.wsdlLocation = wsdlLocation;
	}
	
	public URL getWsdlLocation() {
		return this.wsdlLocation;
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
	
	public void setSecurityMetaData(SecurityMetaData securityMetaData) {
		this.securityMetaData = securityMetaData;
	}
	
	public SecurityMetaData getSecurityMetaData() {
		return this.securityMetaData;
	}
	
	public String toString() {
		return "[RubyWebServiceMetaData: name=" + this.name + "; wsdlLocation=" + this.wsdlLocation + "; endpointClassName=" + this.endpointClassName + "]";
	}

}

