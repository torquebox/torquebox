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

package org.torquebox.web.rack.processors;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.Logger;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.projectodd.polyglot.core.util.TimeInterval;
import org.torquebox.core.processors.AbstractSplitYamlParsingProcessor;
import org.torquebox.web.rack.RackMetaData;

public class WebYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public WebYamlParsingProcessor() {
        setSectionName( "web" );
        setSupportsStandalone( false );
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        if (DeploymentUtils.isUnitRootless( phaseContext.getDeploymentUnit() )) {
            return;
        }
        super.deploy( phaseContext );
    }

    @SuppressWarnings("unchecked")
    public void parse(DeploymentUnit unit, Object dataObj) throws Exception {

        RackMetaData rackAppMetaData = unit.getAttachment( RackMetaData.ATTACHMENT_KEY );

        if (rackAppMetaData == null) {
            rackAppMetaData = new RackMetaData();
            rackAppMetaData.attachTo( unit );
        }

        Map<String, Object> webData = (Map<String, Object>) dataObj;

        rackAppMetaData.setContextPath( (String) webData.get( "context" ) );
        rackAppMetaData.setStaticPathPrefix( (String) webData.get( "static" ) );

        if (webData.get( "rackup" ) != null) {
            rackAppMetaData.setRackUpScriptLocation( (String) webData.get( "rackup" ) );
        }

        Object hosts = webData.get( "host" );

        if (hosts instanceof List) {
            List<String> list = (List<String>) hosts;
            for (String each : list) {
                rackAppMetaData.addHost( each );
            }
        } else {
            rackAppMetaData.addHost( (String) hosts );
        }

        String timeoutStr = null;
        
        if (webData.containsKey( "session-timeout" )) {
            timeoutStr = webData.get( "session-timeout" ).toString(); 
        } else if (webData.containsKey( "session_timeout" )) {
            timeoutStr = webData.get( "session_timeout" ).toString(); 
        }

        rackAppMetaData.setSessionTimeout( TimeInterval.parseInterval( timeoutStr, TimeUnit.MINUTES ) );
    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.web.rack" );

}
