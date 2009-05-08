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

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.ruby.core.runtime.DefaultRubyDynamicClassLoader;
import org.torquebox.ruby.core.runtime.spi.RubyDynamicClassLoader;
import org.torquebox.ruby.enterprise.endpoints.databinding.RubyTypeSpace;
import org.torquebox.ruby.enterprise.endpoints.metadata.RubyEndpointMetaData;

public class RubyTypeSpaceDeployer extends AbstractDeployer {

	private static final String PREFIX = "jboss.ruby.databinding.";

	public RubyTypeSpaceDeployer() {
		setStage(DeploymentStages.POST_CLASSLOADER);
		setAllInputs(true);
		addOutput(BeanMetaData.class);
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		Set<? extends RubyEndpointMetaData> allMetaData = unit.getAllMetaData(RubyEndpointMetaData.class);

		if (allMetaData.size() == 0) {
			return;
		}

		log.debug("deploying for: " + unit);

		BeanMetaData busMetaData = unit.getAttachment(BeanMetaData.class + "$cxf.bus", BeanMetaData.class);

		RubyDynamicClassLoader classLoader = unit.getAttachment(DefaultRubyDynamicClassLoader.class);

		for (RubyEndpointMetaData endpointMetaData : allMetaData) {
			String beanName = getBeanName(unit, endpointMetaData.getName());
			BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanName, RubyTypeSpace.class.getName());

			builder.addPropertyMetaData("rubyPath", "jboss/databinding/" + endpointMetaData.getName() );
			builder.addPropertyMetaData("wsdlLocation", endpointMetaData.getWsdlLocation());
			builder.addPropertyMetaData("rubyDynamicClassLoader", classLoader);
			
			ValueMetaData busInjection = builder.createInject(busMetaData.getName());
			builder.addPropertyMetaData("bus", busInjection);

			BeanMetaData beanMetaData = builder.getBeanMetaData();
			unit.addAttachment(BeanMetaData.class.getName() + "$databinding." + endpointMetaData.getName(), beanMetaData, BeanMetaData.class);
		}
	}

	public static String getBeanName(DeploymentUnit unit, String serviceName) {
		return PREFIX + unit.getSimpleName() + "." + serviceName;
	}

}
