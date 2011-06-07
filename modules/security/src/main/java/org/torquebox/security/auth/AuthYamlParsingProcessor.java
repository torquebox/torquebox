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

package org.torquebox.security.auth;

import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.torquebox.core.AbstractSplitYamlParsingProcessor;

public class AuthYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public AuthYamlParsingProcessor() {
        setSectionName( "auth" );
        setSupportsStandalone( true );
    }

    @Override
    protected void parse(DeploymentUnit unit, Object dataObject) throws Exception {
        log.info( "parsing: " + dataObject );

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) dataObject;

        if (data != null) {
            for (String name : data.keySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) data.get( name );
                log.info( "Loading auth configuration for " + name + ":" + config.get( "domain" ) );
                AuthMetaData metaData = new AuthMetaData();
                metaData.addAuthentication( name, config );
                unit.addToAttachmentList( AuthMetaData.ATTACHMENT_KEY, metaData );
            }
        } else {
            log.info( "No jaas auth configured. Moving on." );
        }
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.auth" );
}
