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
package org.torquebox.ruby.core.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jruby.Ruby;
import org.jruby.RubyModule;

public abstract class AbstractRubyIntrospectingDeployer<T> extends AbstractDeployer {

	public AbstractRubyIntrospectingDeployer() {
		setStage(DeploymentStages.POST_CLASSLOADER);
	}

	protected void introspect(DeploymentUnit unit, T metaData, String className, String classLocation) throws DeploymentException {
		Ruby ruby = getRubyRuntime(unit);
		
		RubyModule module = ruby.getClassFromPath( className );
		
		if ( module == null ) {
			throw new DeploymentException( "No such ruby class '" + className + "'" );
		}
		
		introspect(unit, metaData, ruby, module );
	}

	protected abstract void introspect(DeploymentUnit unit, T metaData, Ruby ruby, RubyModule module) throws DeploymentException;

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
		
		while ( cur != null ) {
			if ( cur.getName().equals( requiredSuper ) ) {
				superFound = true;
				break;
			}
			cur = cur.getSuperClass();
		}
		
		if ( ! superFound) {
			throw new DeploymentException( module.getName() + " does not inherit from " + requiredSuper );
		}
	}

}
