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
package org.torquebox.ruby.enterprise.web.rack.deployers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.ruby.core.runtime.metadata.PoolMetaData;
import org.torquebox.ruby.core.runtime.metadata.PoolingMetaData;
import org.torquebox.ruby.enterprise.web.rack.DefaultRackApplicationPool;
import org.torquebox.ruby.enterprise.web.rack.SharedRackApplicationPool;
import org.torquebox.ruby.enterprise.web.rack.metadata.RubyRackApplicationMetaData;
import org.torquebox.ruby.enterprise.web.rack.spi.RackApplicationFactory;

public class RubyRackApplicationPoolDeployer extends AbstractSimpleVFSRealDeployer<RubyRackApplicationMetaData> {

	public RubyRackApplicationPoolDeployer() {
		super(RubyRackApplicationMetaData.class);
		addInput(PoolingMetaData.class);
		addOutput(BeanMetaData.class);
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, RubyRackApplicationMetaData metaData) throws DeploymentException {

		PoolingMetaData pooling = unit.getAttachment(PoolingMetaData.class);

		if ( pooling == null ) {
			deploySharedPool( unit );
		} else {
			PoolMetaData pool = pooling.getPool( "web" );
			
			if (pool == null || pool.isGlobal() || pool.isShared() ) {
				deploySharedPool( unit );
			} else {
				deployDefaultPool(unit, pool);
			}
		}

	}
	
	protected void deploySharedPool(VFSDeploymentUnit unit) throws DeploymentException {
		String beanName = getBeanName(unit);
		BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanName, SharedRackApplicationPool.class.getName());

		String factoryBeanName = RubyRackApplicationFactoryDeployer.getBeanName(unit);
		ValueMetaData appFactoryInjection = builder.createInject(factoryBeanName);
		builder.addConstructorParameter(RackApplicationFactory.class.getName(), appFactoryInjection);

		BeanMetaData beanMetaData = builder.getBeanMetaData();

		unit.addAttachment(BeanMetaData.class.getName() + "$" + beanName, beanMetaData);
	}

	protected void deployDefaultPool(VFSDeploymentUnit unit, PoolMetaData metaData) throws DeploymentException {
		String beanName = getBeanName(unit);
		BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanName, DefaultRackApplicationPool.class
				.getName());

		String factoryBeanName = RubyRackApplicationFactoryDeployer.getBeanName(unit);
		ValueMetaData appFactoryInjection = builder.createInject(factoryBeanName);
		builder.addConstructorParameter(RackApplicationFactory.class.getName(), appFactoryInjection);
		builder.addPropertyMetaData("minInstances", metaData.getMinimumSize());
		builder.addPropertyMetaData("maxInstances", metaData.getMaximumSize());

		BeanMetaData beanMetaData = builder.getBeanMetaData();

		unit.addAttachment(BeanMetaData.class.getName() + "$" + beanName, beanMetaData);
	}

	public static String getBeanName(VFSDeploymentUnit unit) {
		return getBeanName( unit.getRoot() );
	}
	
	public static String getBeanName(VirtualFile file) {
		return getBeanName( file.getName() );
	}
	
	public static String getBeanName(String base) {
		return "torquebox.rack.app.pool." + base;
	}

}
