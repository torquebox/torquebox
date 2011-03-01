/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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
package org.torquebox.base.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.AuthMetaData;
import org.torquebox.base.metadata.RubyApplicationMetaData;

public class AuthDefaultsDeployer extends AbstractDeployer
{
	public static final String DEFAULT_STRATEGY = "file";
	public static final String DEFAULT_DOMAIN   = "other";

	public AuthDefaultsDeployer() {
        setInput( RubyApplicationMetaData.class );
        addInput( AuthMetaData.class );
        addOutput( AuthMetaData.class );
        setStage( DeploymentStages.POST_PARSE );
	}
	
	@Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
		AuthMetaData authMetaData = unit.getAttachment( AuthMetaData.class );
		if (authMetaData == null) {
			log.warn("No authentication configuration found. Configuring TorqueBox Authentication");
			authMetaData = new AuthMetaData();
			unit.addAttachment(AuthMetaData.class, authMetaData);
		}
		if (authMetaData.getConfigurations().size() < 1) {
    		log.warn("No auth configuration provided. Using defaults.");
    		authMetaData.addAuthentication("default", AuthDefaultsDeployer.DEFAULT_DOMAIN, AuthDefaultsDeployer.DEFAULT_STRATEGY);
		}
    }
}
