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
package org.torquebox.ruby.core.runtime.deployers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.ruby.core.runtime.SharedRubyRuntimePool;
import org.torquebox.ruby.core.runtime.metadata.RubyRuntimeMetaData;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimeFactory;

public class RubyRuntimePoolDeployer extends AbstractSimpleVFSRealDeployer<RubyRuntimeMetaData> {
	
	public RubyRuntimePoolDeployer() {
		super( RubyRuntimeMetaData.class );
		addOutput(BeanMetaData.class);
		setStage(DeploymentStages.CLASSLOADER );
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, RubyRuntimeMetaData metaData) throws DeploymentException {
		String beanName = getBeanName( unit );
		log.debug( "creating RubyRuntimePool: " + beanName );
		BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder( beanName, 
				SharedRubyRuntimePool.class.getName() );
		ValueMetaData factoryInjection = builder.createInject( "jboss.ruby.runtime.factory." + unit.getSimpleName() );
		builder.addConstructorParameter( RubyRuntimeFactory.class.getName(), factoryInjection );
		BeanMetaData poolBean = builder.getBeanMetaData();
		unit.addAttachment( BeanMetaData.class.getName() + "$RubyRuntimePool", poolBean, BeanMetaData.class );
		
	}
	
	public static String getBeanName(DeploymentUnit unit) {
		return "jboss.ruby.runtime.pool." + unit.getName();
	}

}
