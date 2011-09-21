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

package org.projectodd.polyglot.core.util;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;

/**
 * Logs deprecation warnings. Useful for deprecating configuration 
 * options.
 * 
 * @author Toby Crawley <tcrawley@redhat.com>
 */
public class DeprecationLogger {
    public static final AttachmentKey<DeprecationLogger> ATTACHMENT_KEY = AttachmentKey.create(DeprecationLogger.class);

    public void append(String message) {
        this.messages.add( message );
    }
    
    public boolean hasMessages() {
        return !this.messages.isEmpty();
    }
    
    public void dumpToLog(Logger log) {
        for(String each : this.messages) {
            log.warn( "WARNING: " + each );
        }
    }
    
    public static DeprecationLogger getLogger(DeploymentUnit unit) {
        DeprecationLogger logger = unit.getAttachment( ATTACHMENT_KEY );
        if (logger == null) {
            logger = new DeprecationLogger();
            unit.putAttachment( ATTACHMENT_KEY, logger );
        }
        
        return logger;
    }
    
    private List<String> messages = new ArrayList<String>();
}
