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

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimeFactory;
import org.torquebox.ruby.enterprise.endpoints.metadata.RubyEndpointMetaData;
import org.torquebox.ruby.enterprise.endpoints.metadata.SecurityMetaData;

public class RubyEndpointIntrospectionDeployer extends AbstractDeployer {

	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

	public RubyEndpointIntrospectionDeployer() {
		setStage(DeploymentStages.POST_CLASSLOADER);
		setAllInputs(true);
		addOutput(RubyEndpointMetaData.class);
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		Set<? extends RubyEndpointMetaData> allMetaData = unit.getAllMetaData(RubyEndpointMetaData.class);

		if (allMetaData.size() == 0) {
			return;
		}

		RubyRuntimeFactory factory = unit.getAttachment(RubyRuntimeFactory.class);

		if (factory == null) {
			throw new DeploymentException("RubyRuntimeFactory is not present.");
		}

		try {
			Ruby runtime = factory.createRubyRuntime();
			
			loadSupport( runtime );

			for (RubyEndpointMetaData each : allMetaData) {
				if (each.getTargetNamespace() == null || each.getPortName() == null) {
					introspect(runtime, each);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeploymentException(e);
		}
	}

	protected void loadSupport(Ruby runtime) {
		String supportScript = "require %q(jboss/endpoints/base_endpoint)\n";
		runtime.evalScriptlet( supportScript );
		
	}

	protected void introspect(Ruby runtime, RubyEndpointMetaData metaData) throws DeploymentException {
		log.debug("introspecting: " + metaData);
		String classLocation = metaData.getClassLocation();

		if (classLocation != null) {
			requireOrLoad(runtime, classLocation);
		}
		
		RubyClass rubyClass = runtime.getClass(metaData.getEndpointClassName());
		log.debug("ruby class is " + rubyClass);
		
		RubyClass cur = rubyClass;
		
		boolean superClassFound = false;
		
		while ( cur != null ) {
			if ( cur.getName().equals( "JBoss::Endpoints::BaseEndpoint" ) ) {
				superClassFound = true;
				break;
			}
			cur = cur.getSuperClass();
		}
		
		if ( ! superClassFound) {
			throw new DeploymentException( "Endpoint " + metaData.getEndpointClassName() + " does not have JBoss::Endpoints::BaseEndpoint as a superclass." );
		}
		
		String targetNamespace = (String) reflect( rubyClass, "target_namespace" );
		String portName        = (String) reflect( rubyClass, "port_name" );
		
		SecurityMetaData securityMetaData = (SecurityMetaData) reflect( rubyClass, "security" );
		
		if ( metaData.getTargetNamespace() == null ) {
			metaData.setTargetNamespace( targetNamespace );
		}
		
		if ( metaData.getPortName() == null ) {
			metaData.setPortName( portName );
		}
		
		if ( metaData.getSecurityMetaData() == null ) {
			metaData.setSecurityMetaData( securityMetaData );
		}
	}
	
	protected Object reflect(IRubyObject obj, String attr) {
		return JavaEmbedUtils.invokeMethod( obj.getRuntime(), obj, attr, EMPTY_OBJECT_ARRAY, Object.class );
	}

	private void requireOrLoad(Ruby runtime, String classLocation) {
		if ( ! classLocation.endsWith( ".rb" ) ) {
			classLocation = classLocation + ".rb";
		}
		String require = "load %q(" + classLocation + ")\n";
		log.debug("eval: " + require);
		runtime.evalScriptlet(require);
	}

}
