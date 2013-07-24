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

package org.torquebox.core.runtime;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.torquebox.core.as.CoreServices;

public class RuntimeRestartScanner implements Service <RuntimeRestartScanner>{

    public RuntimeRestartScanner() {
        this.scheduledExecutor = Executors.newScheduledThreadPool( 1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread( runnable );
                thread.setName( "torquebox-runtime-restart-scanner" );
                return thread;
            }

        } );
    }

    @Override
    public RuntimeRestartScanner getValue() {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.scheduledExecutor.scheduleWithFixedDelay( new Runnable() {
            @Override
            public void run() {
                try {
                    scan();
                } catch (Exception e) {
                    log.error( "Error while scanning for runtime restart markers", e );
                }
            }
        }, 0, scanInterval, TimeUnit.MILLISECONDS );
    }

    @Override
    public void stop(StopContext context) {
        this.scheduledExecutor.shutdown();
    }

    public void addDeploymentUnit(DeploymentUnit unit) {
        this.deploymentUnits.add( unit );
    }

    public void removeDeploymentUnit(DeploymentUnit unit) {
        this.deploymentUnits.remove( unit );
    }

    protected void scan() {
        Iterator<DeploymentUnit> iter = this.deploymentUnits.iterator();
        while (iter.hasNext()) {
            DeploymentUnit unit = iter.next();
            log.trace( "Scanning " + unit.getName() + " for restart markers" );
            checkForRestartMarker( unit, "restart.txt", "web" );
            checkForRestartMarker( unit, "restart-web.txt", "web" );
            checkForRestartMarker( unit, "restart-jobs.txt", "jobs" );
            checkForRestartMarker( unit, "restart-messaging.txt", "messaging" );
            checkForRestartMarker( unit, "restart-services.txt", "services" );
            checkForRestartMarker( unit, "restart-stomplets.txt", "stomplets" );
            checkForRestartMarker( unit, "restart-all.txt", "web", "jobs", "messaging", "services", "stomplets" );
        }
    }

    @SuppressWarnings("rawtypes")
    private void checkForRestartMarker(DeploymentUnit unit, String markerFile, String... poolNames) {
        String deploymentRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT ).getRoot().getPathName();
        File redeployMarker = new File( deploymentRoot + "/tmp/" + markerFile );
        if (redeployMarker.exists()) {
            log.debug( "Found restart marker " + redeployMarker );
            for (String poolName : poolNames) {
                ServiceController serviceController = unit.getServiceRegistry().getService( CoreServices.runtimePoolName( unit, poolName ) );
                if (serviceController != null) {
                    log.info( "Restarting " + poolName + " runtime for " + unit.getName() );
                    RestartableRubyRuntimePool pool = (RestartableRubyRuntimePool) serviceController.getValue();
                    try {
                        pool.restart();
                    } catch (Exception e) {
                        log.error( "Error restarting " + poolName + " runtime for " + unit.getName(), e );
                    }
                }
            }
            redeployMarker.delete();
        }
    }

    private ScheduledExecutorService scheduledExecutor;
    private int scanInterval = 2000; // in milliseconds
    private CopyOnWriteArraySet<DeploymentUnit> deploymentUnits = new CopyOnWriteArraySet<DeploymentUnit>();
    private static final Logger log = Logger.getLogger( "org.torquebox.core.runtime" );

}
