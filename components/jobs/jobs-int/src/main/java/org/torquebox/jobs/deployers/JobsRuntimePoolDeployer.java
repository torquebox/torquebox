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

package org.torquebox.jobs.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.jobs.metadata.ScheduledJobMetaData;
import org.torquebox.mc.AttachmentUtils;

/**
 * <pre>
 * Stage: DESCRIBE
 *    In: EnvironmentMetaData, PoolMetaData, ScheduledJobMetaData
 *   Out: PoolMetaData
 * </pre>
 * 
 * Ensures that pool metadata for jobs is available
 */
public class JobsRuntimePoolDeployer extends AbstractDeployer {

    /**
     * I'd rather use setInput(ScheduledJobMetaData) and omit the getAllMetaData
     * short circuit in deploy(), but that requires attachers to pass an
     * ExpectedType, and I don't think we can assume that.
     */
    public JobsRuntimePoolDeployer() {
        setStage( DeploymentStages.DESCRIBE );
        addInput( ScheduledJobMetaData.class );
        addInput( RubyApplicationMetaData.class );
        addInput( PoolMetaData.class );
        addOutput( PoolMetaData.class );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit.getAllMetaData( ScheduledJobMetaData.class ).isEmpty()) {
            return;
        }
        PoolMetaData jobsPool = AttachmentUtils.getAttachment( unit, "jobs", PoolMetaData.class );
        ;
        if (jobsPool == null) {
            RubyApplicationMetaData envMetaData = unit.getAttachment( RubyApplicationMetaData.class );
            boolean devMode = envMetaData != null && envMetaData.isDevelopmentMode();
            jobsPool = devMode ? new PoolMetaData( "jobs", 1, 2 ) : new PoolMetaData( "jobs" );
            log.info( "Configured Ruby runtime pool for jobs: " + jobsPool );
            AttachmentUtils.multipleAttach( unit, jobsPool, "jobs" );
        }
    }

}
