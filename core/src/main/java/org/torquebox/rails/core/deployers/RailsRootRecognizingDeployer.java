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
package org.torquebox.rails.core.deployers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.rails.core.metadata.RailsApplicationMetaData;

public class RailsRootRecognizingDeployer extends AbstractDeployer {
	
	public RailsRootRecognizingDeployer() {
		setAllInputs( true );
		addOutput( RailsApplicationMetaData.class );
		setStage(DeploymentStages.NOT_INSTALLED );
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		if ( unit.getAttachment( RailsApplicationMetaData.class ) != null ) {
			return;
		}
		
		if ( unit instanceof VFSDeploymentUnit ) {
			deploy( (VFSDeploymentUnit) unit );
		}
	}
	
	public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
		VirtualFile root = unit.getRoot();
		
		try {
			if ( root.getChild( "config/environment.rb" ) != null ) {
				log.debug( "attaching: " + unit );
				RailsApplicationMetaData railsAppMetaData = new RailsApplicationMetaData( root );
				unit.addAttachment( RailsApplicationMetaData.class, railsAppMetaData );
			}
		} catch (IOException e) {
			throw new DeploymentException( e );
		} catch (URISyntaxException e) {
			throw new DeploymentException( e );
		}
	}

}
