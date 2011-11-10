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

package org.torquebox.core;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class DeploymentScannerHelper implements Service<DeploymentScannerHelper> {

    @Override
    public DeploymentScannerHelper getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        File deploymentDir = new File( injectedPath.getValue() );
        convertFailedMarkersToDoDeployMarkers( deploymentDir );
    }

    protected void convertFailedMarkersToDoDeployMarkers(File directory) {
        for (File failedMarker : directory.listFiles( failedFilter )) {
            convertFailedMarkerToDoDeployMarker( failedMarker );
        }
    }

    protected void convertFailedMarkerToDoDeployMarker(File failedMarker) {
        File doDeployMarker = doDeployMarkerFromFailedMarker( failedMarker );
        failedMarker.renameTo( doDeployMarker );
    }

    protected File doDeployMarkerFromFailedMarker(File failedMarker) {
        return new File( failedMarker.getPath().replace( FAILED_DEPLOY, DO_DEPLOY ) );
    }

    @Override
    public void stop(StopContext context) {
    }

    public InjectedValue<String> getPathInjector() {
        return injectedPath;
    }

    protected static FilenameFilter failedFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return failedPattern.matcher( name ).matches();
        }
    };

    private InjectedValue<String> injectedPath = new InjectedValue<String>();
    protected static final String FAILED_DEPLOY = ".failed";
    protected static final String DO_DEPLOY = ".dodeploy";
    private static final Pattern failedPattern = Pattern.compile( ".+-knob\\.yml\\" + FAILED_DEPLOY );
}
