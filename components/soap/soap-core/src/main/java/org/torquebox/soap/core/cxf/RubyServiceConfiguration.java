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
package org.torquebox.soap.core.cxf;

import java.lang.reflect.Method;

import javax.xml.transform.stax.StAXSource;

import org.apache.cxf.service.factory.DefaultServiceConfiguration;

public class RubyServiceConfiguration extends DefaultServiceConfiguration {
	
	private String portName;

	public RubyServiceConfiguration(String portName) {
		this.portName = portName;
	}

	@Override
	public String getServiceName() {
		return this.portName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getRequestWrapper(Method selected) {
		return StAXSource.class;
	}

	@Override
	public String getRequestWrapperClassName(Method selected) {
		return StAXSource.class.getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getResponseWrapper(Method selected) {
		return StAXSource.class;
	}

	@Override
	public String getResponseWrapperClassName(Method selected) {
		return StAXSource.class.getName();
	}

}
