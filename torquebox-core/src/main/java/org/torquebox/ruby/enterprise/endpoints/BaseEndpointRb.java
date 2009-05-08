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

import java.security.Principal;

import org.jboss.logging.Logger;

public class BaseEndpointRb {
	
	private Logger logger;
	private Principal principal;
	private Object request;
	private String responseCreator;
	

	public BaseEndpointRb() {
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public Logger getLogger() {
		return this.logger;
	}

	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}
	
	public Principal getPrincipal() {
		return this.principal;
	}

	public void setRequest(Object request) {
		this.request = request;
	}
	
	public Object getRequest() {
		return this.request;
	}

	public void setResponseCreator(String responseCreator) {
		this.responseCreator = responseCreator;
	}
	
	public String getResponseCreator() {
		return this.responseCreator;
	}

	public String toString() {
		return "[BaseEndpointRb: principal=" + principal + "; request=" + request + "; responseCreator=" + responseCreator + "]";
	}

}
