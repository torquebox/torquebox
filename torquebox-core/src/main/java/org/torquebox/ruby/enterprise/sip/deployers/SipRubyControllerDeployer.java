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
package org.torquebox.ruby.enterprise.sip.deployers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.ruby.core.runtime.deployers.RubyRuntimePoolDeployer;
import org.torquebox.ruby.enterprise.sip.metadata.SipApplicationMetaData;
import org.torquebox.ruby.enterprise.sip.metadata.SipRubyControllerMetaData;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class SipRubyControllerDeployer extends
	AbstractSimpleVFSRealDeployer<SipApplicationMetaData> {

	/**
	 * @param output
	 */
	public SipRubyControllerDeployer() {
		super(SipApplicationMetaData.class);
		setStage(DeploymentStages.PRE_REAL);
		setRelativeOrder(1000);
		addOutput(BeanMetaData.class);
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, SipApplicationMetaData metaData)
			throws DeploymentException {
		
		log.debug("deploying controller: " + SipRubyControllerMetaData.class.getName());

		BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(SipRubyControllerMetaData.class.getName(), SipRubyControllerMetaData.class.getName());

		builder.addPropertyMetaData("name", metaData.getRubyController());
		
		ValueMetaData poolInjection = builder.createInject(RubyRuntimePoolDeployer.getBeanName(unit));
		builder.addPropertyMetaData("rubyRuntimePool", poolInjection);

		BeanMetaData beanMetaData = builder.getBeanMetaData();

		unit.addAttachment(SipRubyControllerMetaData.class.getName(), beanMetaData, BeanMetaData.class);
	}

}
