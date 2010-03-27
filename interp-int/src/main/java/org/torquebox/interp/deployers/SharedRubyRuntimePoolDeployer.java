/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.common.runtime.SharedRubyRuntimePool;
import org.torquebox.interp.metadata.SharedRubyRuntimePoolMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;

public class SharedRubyRuntimePoolDeployer extends AbstractSimpleRealDeployer<SharedRubyRuntimePoolMetaData> {

	
	public SharedRubyRuntimePoolDeployer() {
		super(SharedRubyRuntimePoolMetaData.class);
		addOutput(BeanMetaData.class);
	}

	@Override
	public void deploy(DeploymentUnit unit, SharedRubyRuntimePoolMetaData deployment) throws DeploymentException {
		BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(deployment.getName(), SharedRubyRuntimePool.class.getName() );
		builder.addConstructorParameter( RubyRuntimeFactory.class.getName(), (Object) null );
		BeanMetaData poolMetaData = builder.getBeanMetaData();
		unit.addAttachment(BeanMetaData.class.getName() + "$SharedRubyRuntimePool_" + deployment.getName(), poolMetaData, BeanMetaData.class);
	}
	

}
