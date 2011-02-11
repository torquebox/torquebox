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

package org.torquebox.services.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.services.ServiceMetaData;

/**
 * <pre>
 * Stage: DESCRIBE
 *    In: EnvironmentMetaData, PoolMetaData, ScheduledJobMetaData
 *   Out: PoolMetaData
 * </pre>
 * 
 * Ensures that pool metadata for jobs is available
 */
public class ServicesRuntimePoolDeployer extends AbstractDeployer {

    public static final String POOL_NAME = "services";

    public ServicesRuntimePoolDeployer() {
        setStage( DeploymentStages.DESCRIBE );
        addInput( ServiceMetaData.class );
        addInput( RubyApplicationMetaData.class );
        addInput( PoolMetaData.class );
        addOutput( PoolMetaData.class );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit.getAllMetaData( ServiceMetaData.class ).isEmpty()) {
            return;
        }

        log.debug( "Deploying ruby runtime pool for services: " + unit );
        PoolMetaData servicesPool = AttachmentUtils.getAttachment( unit, "services", PoolMetaData.class );

        if (servicesPool == null) {
            log.debug( "Configuring ruby runtime pool for services: " + unit );
            servicesPool = new PoolMetaData( "services" );
            AttachmentUtils.multipleAttach( unit, servicesPool, "services" );
        }
    }

}
