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
package org.torquebox.interp.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public abstract class AbstractRubyIntrospectingDeployer<T> extends AbstractDeployer {

	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};

	public AbstractRubyIntrospectingDeployer() {
		setStage(DeploymentStages.POST_CLASSLOADER);
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		if ( unit instanceof VFSDeploymentUnit ) {
			deploy( (VFSDeploymentUnit) unit );
		}
		
	}
	
	protected abstract void deploy(VFSDeploymentUnit unit) throws DeploymentException;

	protected void introspect(VFSDeploymentUnit unit, T metaData, String className, String classLocation) throws DeploymentException {
		log.info("ABout to introspect [" + className + "] at [" + classLocation + "]");
		Ruby ruby = getRubyRuntime(unit);

		if (classLocation != null) {
			log.info("Loading [" + classLocation + "]");
			ruby.evalScriptlet("load %(" + classLocation + ".rb)\n");
		}

		log.info("Grab class [" + className + "]");
		RubyModule module = ruby.getClassFromPath(className);

		if (module == null) {
			throw new DeploymentException("No such ruby class '" + className + "'");
		}

		introspect(unit, metaData, ruby, module);
		log.info( "completely introspected" );
	}

	protected abstract void introspect(VFSDeploymentUnit unit, T metaData, Ruby ruby, RubyModule module) throws DeploymentException;

	protected Ruby getRubyRuntime(DeploymentUnit unit) throws DeploymentException {
		Ruby ruby = unit.getAttachment(Ruby.class);
		if (ruby == null) {
			throw new DeploymentException("No ruby runtime available");
		}
		return ruby;
	}

	protected void ensureHierarchy(RubyModule module, String requiredSuper) throws DeploymentException {
		RubyModule cur = module;

		boolean superFound = false;

		while (cur != null) {
			if (cur.getName().equals(requiredSuper)) {
				superFound = true;
				break;
			}
			cur = cur.getSuperClass();
		}

		if (!superFound) {
			throw new DeploymentException(module.getName() + " does not inherit from " + requiredSuper);
		}
	}

	protected Object reflect(IRubyObject obj, String attr) {
		return JavaEmbedUtils.invokeMethod(obj.getRuntime(), obj, attr, EMPTY_OBJECT_ARRAY, Object.class);
	}

	protected boolean respondTo(IRubyObject obj, String message) {
		Boolean result = (Boolean) JavaEmbedUtils.invokeMethod(obj.getRuntime(), obj, "respond_to?", new Object[] { message },
				Boolean.class);

		if (result != null) {
			return result.booleanValue();
		}

		return false;
	}

}
