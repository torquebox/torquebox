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
package org.torquebox.rack.deployers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.rack.core.DefaultRackApplicationPool;
import org.torquebox.rack.core.RubyRackApplicationFactory;
import org.torquebox.rack.core.SharedRackApplicationPool;
import org.torquebox.rack.metadata.RubyRackApplicationMetaData;
import org.torquebox.rack.spi.RackApplicationFactory;
import org.torquebox.rack.spi.RackApplicationPool;

public class RubyRackApplicationPoolDeployer extends AbstractSimpleVFSRealDeployer<RubyRackApplicationMetaData> {

	public RubyRackApplicationPoolDeployer() {
		super(RubyRackApplicationMetaData.class);
		addInput(PoolMetaData.class);
		addOutput(BeanMetaData.class);
		setStage( DeploymentStages.POST_CLASSLOADER );
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, RubyRackApplicationMetaData metaData) throws DeploymentException {

		System.err.println( "RubyRackApplicationPoolDeployer.deploy(" + unit + ")" );
		PoolMetaData pool = getPoolMetaData(unit, "web");

		if (pool == null || pool.isGlobal() || pool.isShared()) {
			deploySharedPool(unit);
		} else {
			deployDefaultPool(unit, pool);
		}

	}

	protected void deploySharedPool(VFSDeploymentUnit unit) throws DeploymentException {
		String beanName = AttachmentUtils.beanName(unit, RackApplicationPool.class );
		BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanName, SharedRackApplicationPool.class.getName());

		String factoryBeanName = AttachmentUtils.beanName(unit, RubyRackApplicationFactory.class );
		ValueMetaData appFactoryInjection = builder.createInject(factoryBeanName);
		builder.addConstructorParameter(RackApplicationFactory.class.getName(), appFactoryInjection);

		AttachmentUtils.attach( unit, builder.getBeanMetaData() );
	}

	protected void deployDefaultPool(VFSDeploymentUnit unit, PoolMetaData metaData) throws DeploymentException {
		String beanName = AttachmentUtils.beanName(unit, RackApplicationPool.class );
		BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanName, DefaultRackApplicationPool.class.getName());

		String factoryBeanName = AttachmentUtils.beanName(unit, RubyRackApplicationFactory.class );
		ValueMetaData appFactoryInjection = builder.createInject(factoryBeanName);
		builder.addConstructorParameter(RackApplicationFactory.class.getName(), appFactoryInjection);
		builder.addPropertyMetaData("minInstances", metaData.getMinimumSize());
		builder.addPropertyMetaData("maxInstances", metaData.getMaximumSize());

		AttachmentUtils.attach( unit, builder.getBeanMetaData() );
	}

	protected static PoolMetaData getPoolMetaData(VFSDeploymentUnit unit, String poolName) {
		for (PoolMetaData each : unit.getAllMetaData(PoolMetaData.class)) {
			if (each.getName().equals(poolName)) {
				return each;
			}
		}

		return null;
	}

}
