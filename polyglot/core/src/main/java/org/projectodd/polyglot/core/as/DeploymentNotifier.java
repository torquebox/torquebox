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

package org.projectodd.polyglot.core.as;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.projectodd.polyglot.core.util.DeprecationLogger;

public class DeploymentNotifier implements Service<Void> {
    
    public static final AttachmentKey<AttachmentList<ServiceName>> SERVICES_ATTACHMENT_KEY = AttachmentKey.createList( ServiceName.class );
    public static final AttachmentKey<Long> DEPLOYMENT_TIME_ATTACHMENT_KEY = AttachmentKey.create( Long.class );
    
    public DeploymentNotifier(DeploymentUnit unit) {
        this.unit = unit;
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    @Override
    public void start(StartContext context) throws StartException {
        long startTime = unit.getAttachment( DeploymentNotifier.DEPLOYMENT_TIME_ATTACHMENT_KEY );
        long elapsed = System.currentTimeMillis() - startTime;
        log.info( "Completely deployed: " + unit.getName() + " in " + elapsed + "ms" );
        
        DeprecationLogger deprecations = unit.getAttachment( DeprecationLogger.ATTACHMENT_KEY );
        if (deprecations != null && deprecations.hasMessages()) {
            log.warn( "The deployment '" + unit.getName() + "' generated the following deprecation warnings:" );    
            deprecations.dumpToLog( log );
        }
    }

    @Override
    public void stop(StopContext context) {
        
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.core.as" );
    
    private DeploymentUnit unit;


}
