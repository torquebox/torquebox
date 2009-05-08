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
package org.torquebox.rails.sip.deployers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.torquebox.rails.core.metadata.RailsApplicationMetaData;
import org.torquebox.ruby.core.runtime.metadata.RubyLoadPathMetaData;

public class RailsSipRubyLoadPathDescriber extends AbstractSimpleVFSRealDeployer<RailsApplicationMetaData> {

	public RailsSipRubyLoadPathDescriber() {
		super(RailsApplicationMetaData.class);
		addOutput(RubyLoadPathMetaData.class);
		setStage(DeploymentStages.DESCRIBE);
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, RailsApplicationMetaData root) throws DeploymentException {
		VirtualFile sipDir;
		try {
			sipDir = unit.getRoot().getChild("app/sip");
			if (sipDir != null) {
				log.debug( "adding sipDir [" + sipDir.toURL() + "]" );
				RubyLoadPathMetaData loadPathMetaData = new RubyLoadPathMetaData();
				loadPathMetaData.setURL( sipDir.toURL() );
				unit.addAttachment( RubyLoadPathMetaData.class.getName() + "$sip", loadPathMetaData, RubyLoadPathMetaData.class );
			}

		} catch (IOException e) {
			// ignore
			return;
		} catch (URISyntaxException e) {
			throw new DeploymentException( e );
		}

	}

}
