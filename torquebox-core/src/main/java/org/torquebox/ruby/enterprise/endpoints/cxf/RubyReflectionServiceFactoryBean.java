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
package org.torquebox.ruby.enterprise.endpoints.cxf;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.jboss.logging.Logger;

public class RubyReflectionServiceFactoryBean extends ReflectionServiceFactoryBean {

	private static final Logger log = Logger.getLogger(RubyReflectionServiceFactoryBean.class);

	protected void initializeWSDLOperations()  {
		InterfaceInfo intf = getInterfaceInfo();

		try {
			Method method = serviceClass.getMethod("invoke", Principal.class, String.class, Object.class, QName.class );
			for (OperationInfo o : intf.getOperations()) {
				initializeWSDLOperation(intf, o, method);
			}
		} catch (SecurityException e) {
			log.error("Unable to initialize WSDL operations", e);
		} catch (NoSuchMethodException e) {
			log.error("Unable to initialize WSDL operations", e);
		}
	}

	protected boolean isWrapped(Method m) {
		return false;
	}
	protected boolean initializeClassInfo(OperationInfo o, Method method, List<String> paramOrder) {
		
		log.debug( "initializeClassInfo(" + o + "...)" );
		log.debug( "op: " + o );
		log.debug( "op unwrapped: " + o.getUnwrappedOperation() );
		//boolean result = super.initializeClassInfo(o , method, paramOrder);
		//log.info( "SUPER: " + result );
		
		for ( MessagePartInfo p : o.getInput().getMessageParts() ) {
			p.setTypeClass( DOMSource.class );
			log.debug( "part: " + p );
		}
		//return result;
		return true;

	}

}
