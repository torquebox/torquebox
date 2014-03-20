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

import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.security.auth.AuthMetaData;
import org.torquebox.security.auth.AuthMetaData.TorqueBoxAuthConfig;

public class AuthDefaultsProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext)
            throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }

        // Only initialize defaults if there already is an auth subsection
        if (!unit.hasAttachment( AuthMetaData.ATTACHMENT_KEY )) {
            return;
        }

        AttachmentList<AuthMetaData> allMetaData = unit.getAttachment( AuthMetaData.ATTACHMENT_KEY );

        for (AuthMetaData authMetaData : allMetaData) {
            // Set defaults for any values that weren't explicitly specified in
            // the YAML
            for (TorqueBoxAuthConfig config : authMetaData.getConfigurations()) {
                if (blank( config.getDomain() )) {
                    log.debug( "No domain specified. Configuring using default: " + DEFAULT_DOMAIN );
                    config.setDomain( DEFAULT_DOMAIN );
                }
            }
        }
    }

    @Override
    public void undeploy(DeploymentUnit arg0) {
        // No-op
    }

    public static final String DEFAULT_DOMAIN = "torquebox";
    static final Logger log = Logger.getLogger( "org.torquebox.auth" );

    private boolean blank(String s) {
        return (s == null || s.equals( "" ));
    }

}
