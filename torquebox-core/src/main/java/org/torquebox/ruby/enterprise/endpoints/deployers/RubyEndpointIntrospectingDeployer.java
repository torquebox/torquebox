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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.torquebox.ruby.core.deployers.AbstractRubyIntrospectingDeployer;
import org.torquebox.ruby.enterprise.endpoints.metadata.RubyEndpointMetaData;
import org.torquebox.ruby.enterprise.endpoints.metadata.RubyEndpointsMetaData;

public class RubyEndpointIntrospectingDeployer extends AbstractRubyIntrospectingDeployer<RubyEndpointMetaData> { 

	private static final String BASE_ENDPOINT_CLASS_NAME = "JBoss::Endpoints::BaseEndpoint";
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

	public RubyEndpointIntrospectingDeployer() {
		setInput(RubyEndpointsMetaData.class);
		addOutput(RubyEndpointsMetaData.class);
	}

	protected void loadSupport(Ruby runtime) {
		String supportScript = "require %q(jboss/endpoints/base_endpoint)\n";
		runtime.evalScriptlet( supportScript );
		
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		RubyEndpointsMetaData endpoints = unit.getAttachment( RubyEndpointsMetaData.class );
		
		for ( RubyEndpointMetaData endpoint : endpoints.getEndpoints()  ) {
			introspect(   unit, endpoint, endpoint.getEndpointClassName(), endpoint.getClassLocation() );
		}
	}

	@Override
	protected void introspect(DeploymentUnit unit, RubyEndpointMetaData metaData, Ruby ruby, RubyModule module) throws DeploymentException {
		ensureHierarchy( module, BASE_ENDPOINT_CLASS_NAME );
	}

}
