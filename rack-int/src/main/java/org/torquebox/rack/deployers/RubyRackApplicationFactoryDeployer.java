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
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jruby.Ruby;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.rack.core.GlobalRubyRackApplicationFactory;
import org.torquebox.rack.core.RubyRackApplicationFactory;
import org.torquebox.rack.metadata.RubyRackApplicationMetaData;

public class RubyRackApplicationFactoryDeployer extends AbstractSimpleVFSRealDeployer<RubyRackApplicationMetaData> {

	public RubyRackApplicationFactoryDeployer() {
		super(RubyRackApplicationMetaData.class);
		addInput(PoolMetaData.class);
		addOutput(BeanMetaData.class);
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, RubyRackApplicationMetaData metaData) throws DeploymentException {
		String beanName = getBeanName(unit);

		PoolMetaData pool = getPoolMetaData(unit, "web");

		BeanMetaData beanMetaData = null;

		if (pool == null || pool.isGlobal()) {
			beanMetaData = createGlobal(unit, metaData);
		} else {
			beanMetaData = createDefault(unit, metaData);
		}

		AttachmentUtils.attach( unit, beanMetaData );
	}

	protected BeanMetaData createGlobal(VFSDeploymentUnit unit, RubyRackApplicationMetaData metaData) throws DeploymentException {
		String beanName = getBeanName(unit);
		Ruby ruby = unit.getAttachment(Ruby.class);
		BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanName, GlobalRubyRackApplicationFactory.class.getName());
		builder.addPropertyMetaData("ruby", ruby);
		builder.addPropertyMetaData("rackUpScript", metaData.getRackUpScript());
		return builder.getBeanMetaData();
	}

	protected BeanMetaData createDefault(VFSDeploymentUnit unit, RubyRackApplicationMetaData metaData) throws DeploymentException {
		String beanName = getBeanName(unit);
		RubyRuntimeFactory factory = unit.getAttachment(RubyRuntimeFactory.class);
		BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanName, RubyRackApplicationFactory.class.getName());
		builder.addPropertyMetaData("rubyRuntimeFactory", factory);
		builder.addPropertyMetaData("rackUpScript", metaData.getRackUpScript());
		return builder.getBeanMetaData();
	}

	public static String getBeanName(VFSDeploymentUnit unit) {
		String beanName = "torquebox.rack.app.factory." + unit.getSimpleName();
		return beanName;
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
