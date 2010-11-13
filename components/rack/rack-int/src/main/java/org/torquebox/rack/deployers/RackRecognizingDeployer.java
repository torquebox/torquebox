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

import java.io.IOException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rack.metadata.WriteOnceRackApplicationMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.rack.core.RackRuntimeInitializer;
import org.torquebox.interp.metadata.PoolMetaData;


/**
 * <pre>
 * Stage: NOT_INSTALLED
 *    In: 
 *   Out: RackApplicationMetaData
 * </pre>
 *
 * Creates metadata if it recognizes a deployment as a Rack
 * application
 */
public class RackRecognizingDeployer extends AbstractDeployer {
	
	public RackRecognizingDeployer() {
		setAllInputs( true );
		addOutput( RackApplicationMetaData.class );
		setStage(DeploymentStages.NOT_INSTALLED );
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		if ( unit.getAttachment( RackApplicationMetaData.class ) != null ) {
			return;
		}
		if ( unit instanceof VFSDeploymentUnit ) {
			deploy( (VFSDeploymentUnit) unit );
		}
	}
	
	public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
		VirtualFile root = unit.getRoot();
		try {
			if ( root.getName().endsWith(".rack") && root.getChild( "config.ru" ).exists() ) {
                log.info("Recognized as Rack app: "+root);
                RackApplicationMetaData rackAppMetaData = new WriteOnceRackApplicationMetaData();
                rackAppMetaData.setRackRoot( root );
                unit.addAttachment( RackApplicationMetaData.class, rackAppMetaData );
			}
		} catch (Exception e) {
			throw new DeploymentException( e );
		}
	}

}
