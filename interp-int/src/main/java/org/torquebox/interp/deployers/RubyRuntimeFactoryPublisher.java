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

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.interp.core.RubyRuntimeFactoryProxy;
import org.torquebox.interp.spi.RubyRuntimeFactory;

/** Deployer which publishes an attached RubyRuntimeFactory.
 * 
 * <p>
 * While possibly not awesome design, we simply proxy the RubyRuntimeFactory
 * used during during some other deployers as a MCBean available to other beans.
 * </p>
 * 
 * @author Bob McWhirter
 */
public class RubyRuntimeFactoryPublisher extends AbstractDeployer {

	public RubyRuntimeFactoryPublisher() {
		addOutput(BeanMetaData.class);
		setStage(DeploymentStages.CLASSLOADER);
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		RubyRuntimeFactory factory = unit.getAttachment(RubyRuntimeFactory.class);

		if (factory == null) {
			return;
		}

		String factoryName = getBeanName(unit);
		BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(factoryName, RubyRuntimeFactoryProxy.class.getName());
		builder.addConstructorParameter(RubyRuntimeFactory.class.getName(), factory);
		BeanMetaData factoryBean = builder.getBeanMetaData();
		unit.addAttachment(BeanMetaData.class.getName() + "$RubyRuntimeFactory", factoryBean, BeanMetaData.class);

	}

	public static String getBeanName(DeploymentUnit unit) {
		String beanName = "jboss.ruby.runtime.factory." + unit.getSimpleName();
		return beanName;

	}

}
