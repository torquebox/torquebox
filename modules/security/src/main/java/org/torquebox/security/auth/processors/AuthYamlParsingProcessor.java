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

package org.torquebox.security.auth.processors;

import java.util.Map;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.Logger;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.processors.AbstractSplitYamlParsingProcessor;
import org.torquebox.security.auth.AuthMetaData;

public class AuthYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public AuthYamlParsingProcessor() {
        setSectionName( "auth" );
        setSupportsStandalone( true );
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        if (DeploymentUtils.isUnitRootless( phaseContext.getDeploymentUnit() )) {
            return;
        }
        super.deploy( phaseContext );
    }

    @Override
    protected void parse(DeploymentUnit unit, Object dataObject) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) dataObject;

        if (data != null) {
            for (String name : data.keySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) data.get( name );
                AuthMetaData metaData = new AuthMetaData();
                metaData.addAuthentication( name, config );
                unit.addToAttachmentList( AuthMetaData.ATTACHMENT_KEY, metaData );
            }
        }
    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.auth" );
}
