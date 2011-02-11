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

package org.torquebox.rack.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.rack.metadata.RackApplicationMetaData;

public class RackApplicationDefaultsDeployer extends AbstractDeployer {

    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_CONTEXT_PATH = "/";

    public RackApplicationDefaultsDeployer() {
        setStage( DeploymentStages.PRE_REAL );
        setInput( RackApplicationMetaData.class );
        addOutput( RackApplicationMetaData.class );
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RackApplicationMetaData metadata = unit.getAttachment( RackApplicationMetaData.class );
        if (metadata.getHosts().isEmpty()) {
            metadata.addHost( DEFAULT_HOST );
        }

        if ((metadata.getContextPath() == null) || (metadata.getContextPath().trim().equals( "" ))) {
            metadata.setContextPath( DEFAULT_CONTEXT_PATH );
        }
    }
}
