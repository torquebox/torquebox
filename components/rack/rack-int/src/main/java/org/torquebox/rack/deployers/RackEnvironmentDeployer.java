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
package org.torquebox.rack.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.metadata.EnvironmentMetaData;
import org.torquebox.rack.metadata.RackApplicationMetaData;


/**
 * <pre>
 * Stage: POST_PARSE
 *    In: RackApplicationMetaData
 *   Out: EnvironmentMetaData
 * </pre>
 *
 * The environment metadata links the Rack/Rails environment to the
 * Jobs and Messaging components, allowing their classes to be
 * automatically reloaded in development mode, for example.
 */
public class RackEnvironmentDeployer extends AbstractDeployer {
    
    public RackEnvironmentDeployer() {
        setStage(DeploymentStages.POST_PARSE);
        setInput(RackApplicationMetaData.class);
        addInput(RackDefaultsDeployer.COMPLETE);
        addOutput(EnvironmentMetaData.class);
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RackApplicationMetaData rackAppMetaData = unit.getAttachment(RackApplicationMetaData.class);
        log.info("RACK_ENV="+rackAppMetaData.getRackEnv());
        EnvironmentMetaData envMetaData = new EnvironmentMetaData();
        String rackEnv = rackAppMetaData.getRackEnv();
        if ( rackEnv != null ) {
            envMetaData.setEnvironmentName( rackEnv );
            envMetaData.setDevelopmentMode( rackEnv.equals( "development" ) );
        } else {
            log.warn("The RACK_ENV is null, check deployer config");
        }
        unit.addAttachment(EnvironmentMetaData.class, envMetaData);
    }
}
