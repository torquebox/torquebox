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

package org.torquebox.interp.deployers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;

/**
 * <pre>
 * Stage: PRE_DESCRIBE
 *    In: RubyRuntimeMetaData
 *   Out: RubyRuntimeMetaData
 * </pre>
 * 
 * Adds its configured loadPaths (via jboss-beans.xml) to the runtime. Used to
 * add app/tasks and app/jobs to rails runtime load path for jobs-int and
 * messaging-int.
 */
public class LoadPathDeployer extends AbstractDeployer {

    private List<String> loadPaths = Collections.EMPTY_LIST;

    public LoadPathDeployer() {
        setStage( DeploymentStages.PRE_DESCRIBE );
        setInput( RubyRuntimeMetaData.class );
        addOutput( RubyRuntimeMetaData.class );
    }

    public void setLoadPaths(List<String> loadPaths) {
        this.loadPaths = loadPaths;
    }

    public List<String> getLoadPaths() {
        return this.loadPaths;
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit instanceof VFSDeploymentUnit) {
            deploy( (VFSDeploymentUnit) unit );
        }
    }

    public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
        for (String path : getLoadPaths()) {
            try {
                URL url = unit.getRoot().getChild( path ).toURL();
                log.debug( "Adding to load path: " + url );
                RubyLoadPathMetaData loadPathMeta = new RubyLoadPathMetaData( url );
                runtimeMetaData.appendLoadPath( loadPathMeta );
            } catch (MalformedURLException e) {
                throw new DeploymentException( e );
            }
        }
    }

}
