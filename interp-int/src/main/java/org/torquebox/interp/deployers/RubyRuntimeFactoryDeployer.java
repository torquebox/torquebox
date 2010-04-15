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

import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.kernel.Kernel;
import org.torquebox.interp.core.RubyRuntimeFactoryImpl;
import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.interp.spi.RuntimeInitializer;
import org.torquebox.mc.AttachmentUtils;

/**
 * Deployer which actually creates a RubyRuntimeFactory and attaches it to the
 * unit.
 * 
 * <p>
 * This CLASSLOADER-stage deployer actually creates an instance of
 * RubyRuntimeFactory and attaches it to the unit.
 * </p>
 * 
 * @author Bob McWhirter
 */
public class RubyRuntimeFactoryDeployer extends AbstractSimpleVFSRealDeployer<RubyRuntimeMetaData> {

	/** Kernel. */
	private Kernel kernel;

	/** Construct. */
	public RubyRuntimeFactoryDeployer() {
		super(RubyRuntimeMetaData.class);
		setStage(DeploymentStages.CLASSLOADER);
		addOutput(BeanMetaData.class);
	}

	/**
	 * Set the kernel.
	 * 
	 * @param kernel
	 *            The kernel.
	 */
	public void setKernel(Kernel kernel) {
		this.kernel = kernel;
	}

	/**
	 * Get the kernel.
	 * 
	 * @return The kernel.
	 */
	public Kernel getKernel() {
		return this.kernel;
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, RubyRuntimeMetaData metaData) throws DeploymentException {
		
		String beanName = AttachmentUtils.beanName(unit, RubyRuntimeFactory.class );
		BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder( beanName, RubyRuntimeFactoryImpl.class.getName() );
		
		builder.addConstructorParameter( RuntimeInitializer.class.getName(), metaData.getRuntimeInitializer() );
		
		List<String> loadPaths = new ArrayList<String>();

		for (RubyLoadPathMetaData loadPath : metaData.getLoadPaths()) {
			loadPaths.add(loadPath.getURL().toExternalForm());
		}
		
		builder.addPropertyMetaData( "loadPaths", loadPaths );
		builder.addPropertyMetaData( "kernel", this.kernel );
		builder.addPropertyMetaData( "applicationName", unit.getSimpleName() );
		builder.addPropertyMetaData( "classLoader", unit.getClassLoader() );

		AttachmentUtils.attach(unit, builder.getBeanMetaData() );

		/*
		try {
			long startTime = System.currentTimeMillis();
			Ruby ruby = factory.create();
			long endTime = System.currentTimeMillis();
			log.info("Ruby runtime initialization took " + ((endTime - startTime) / 1000) + "s");
			unit.addAttachment(Ruby.class, ruby);
		} catch (Exception e) {
			throw new DeploymentException(e);
		}
		*/

	}

}
