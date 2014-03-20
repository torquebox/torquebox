/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.app.processors;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.projectodd.polyglot.core.processors.BaseAppJarScanningProcessor;
import org.projectodd.polyglot.core.util.DeploymentUtils;

public class AppJarScanningProcessor extends BaseAppJarScanningProcessor {

    public AppJarScanningProcessor() {
        super( SCAN_ROOTS );
    }

    @SuppressWarnings("serial")
    private static final List<String> SCAN_ROOTS = new ArrayList<String>() {
        {
            add( "lib" );
            add( "vendor/jars" );
            add( "vendor/plugins" );
        }
    };

    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        if (DeploymentUtils.isUnitRootless( phaseContext.getDeploymentUnit() )) {
            return;
        }
        super.deploy( phaseContext );
    }
    
}
