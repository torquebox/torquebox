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

import javax.xml.namespace.QName;

import org.apache.cxf.interceptor.Fault;
import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.common.util.StringUtils;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimePool;
import org.torquebox.ruby.enterprise.endpoints.databinding.RubyType;
import org.torquebox.ruby.enterprise.endpoints.databinding.RubyTypeSpace;

/**
 * Handler bean for dispatching to Ruby classes.
 * 
 * @author Bob McWhirter
 */
public class RubyEndpointHandler {

	private static final Logger log = Logger.getLogger(RubyEndpointHandler.class);

	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};

	private RubyRuntimePool runtimePool;

	private Logger endpointLogger;
	
	private String classLocation;
	private String endpointClassName;

	private RubyTypeSpace typeSpace;

	public RubyEndpointHandler(RubyRuntimePool runtimePool, String classLocation, String endpointClassName, RubyTypeSpace typeSpace) {
		this.endpointLogger = Logger.getLogger( "torquebox.endpoints." + endpointClassName );
		this.runtimePool = runtimePool;
		this.classLocation = classLocation;
		this.endpointClassName = endpointClassName;
		this.typeSpace = typeSpace;
	}

	public Object invoke(Principal principal, String operationName, Object request, QName responseTypeName) {
		log.trace("invoke(" + operationName + ", " + request + ", " + responseTypeName + ")");
		RubyType responseType = typeSpace.getTypeByQName(responseTypeName);
		String responseCreator = null;
		if (responseType != null) {
			responseCreator = responseType.getNewInstanceFragment();
		}

		Ruby ruby = null;

		Object response = null;

		try {
			ruby = runtimePool.borrowRuntime();

			loadEndpointClassLocation(ruby);

			RubyClass endpointClass = ruby.getClass(this.endpointClassName);

			IRubyObject endpoint = createEndpoint(endpointClass);
			inject(endpoint, principal, request, responseCreator);

			response = invoke(endpoint, operationName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Fault(e);
		} finally {
			if (ruby != null) {
				runtimePool.returnRuntime(ruby);
			}
		}
		return response;
	}

	protected IRubyObject createEndpoint(RubyClass endpointClass) {
		return (IRubyObject) JavaEmbedUtils.invokeMethod(endpointClass.getRuntime(), endpointClass, "new", EMPTY_OBJECT_ARRAY, Object.class );
	}

	private void inject(IRubyObject endpoint, Principal principal, Object request, String responseCreator) {
		log.trace("inject(" + endpoint + ", ...)");
		inject( endpoint, "principal=", principal );
		inject( endpoint, "request=", request );
		inject( endpoint, "response_creator=", responseCreator );
		inject( endpoint, "logger=", this.endpointLogger );
	}
	
	private void inject(IRubyObject endpoint, String meth, Object value) {
		Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod( endpoint.getRuntime(), endpoint, "respond_to?", new Object[] { meth }, Boolean.class );
		
		if ( respondTo != null ) {
			if ( respondTo.booleanValue() ) {
				JavaEmbedUtils.invokeMethod( endpoint.getRuntime(), endpoint, meth, new Object[] { value }, void.class );
			}
		}
	}

	private Object invoke(IRubyObject endpoint, String operationName) {
		String methodName = StringUtils.underscore(operationName);
		log.trace("invoke(" + endpoint + ", " + operationName + ") [" + methodName + "]");
		Object response = JavaEmbedUtils.invokeMethod(endpoint.getRuntime(), endpoint, methodName, EMPTY_OBJECT_ARRAY, Object.class);
		log.trace("response is: " + response);
		return response;
	}

	private void loadEndpointClassLocation(Ruby ruby) {
		if (this.classLocation == null) {
			log.debug("no classLocation, not loading");
			return;
		}
		String load = "load %q(" + this.classLocation + ".rb)\n";
		ruby.evalScriptlet(load);
	}

}
