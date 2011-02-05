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
package org.torquebox.rails.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.rails.core.RailsRuntimeInitializer;

/**
 * <pre>
 * Stage: DESCRIBE
 *    In: RubyRuntimeMetaData
 *   Out: RubyRuntimeMetaData
 * </pre>
 * 
 * I'm not entirely sure this can justify its existence. I'd rather merge this
 * behavior into RailsRubyRuntimeFactoryDescriber, it needs to deploy in a later
 * stage than LoadPathDeployer.
 */
public class RailsAutoloadPathDeployer extends AbstractDeployer {

    public RailsAutoloadPathDeployer() {
        setStage( DeploymentStages.DESCRIBE );
        setInput( RubyRuntimeMetaData.class );
        addOutput( RubyRuntimeMetaData.class );
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit instanceof VFSDeploymentUnit) {
            deploy( (VFSDeploymentUnit) unit );
        }
    }

    public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
        if (runtimeMetaData != null && runtimeMetaData.getRuntimeInitializer() instanceof RailsRuntimeInitializer) {
            RailsRuntimeInitializer initializer = (RailsRuntimeInitializer) runtimeMetaData.getRuntimeInitializer();
            for (RubyLoadPathMetaData path : runtimeMetaData.getLoadPaths()) {
                if (path.isAutoload()) {
                    initializer.addAutoloadPath( path.toString() );
                }
            }
        }
    }

}
