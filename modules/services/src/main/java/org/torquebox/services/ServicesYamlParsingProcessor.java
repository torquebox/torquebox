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

package org.torquebox.services;

import java.util.Map;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;
import org.torquebox.core.AbstractSplitYamlParsingProcessor;
import org.torquebox.core.util.StringUtils;

public class ServicesYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public ServicesYamlParsingProcessor() {
        setSectionName( "services" );
        setSupportsStandalone( false );
    }

    @SuppressWarnings("unchecked")
    protected void parse(DeploymentUnit unit, Object dataObj) throws Exception {
        log.info( "parsing: " + dataObj );

        Map<String, Map<String, Object>> results = (Map<String, Map<String, Object>>) dataObj;
        if (results != null) {
            for (String service : results.keySet()) {
                Map<String, Object> params = results.get( service );
                ServiceMetaData serviceMetaData = new ServiceMetaData();
                boolean requiresSingleton = requiresSingleton(  params );
                serviceMetaData.setRequiresSingleton( requiresSingleton );

                String className = service;
                Map<String, Object> config = null;

                if (params != null) {
                    if (params.containsKey( "service" )) {
                        className = (String)params.remove( "service" );
                    }

                    if (params.containsKey( "config" )) {
                        config = (Map<String, Object>)params.remove( "config" );
                    } else {
                        if (!params.isEmpty()) {
                            log.warn( "Use the config: key to pass configuration to a service - the method you are using has been deprecated (service: " + service + ")" );
                        }
                        config = params;
                    } 
                }

                serviceMetaData.setClassName( className );
                serviceMetaData.setName( service );
                serviceMetaData.setParameters( config );
                serviceMetaData.setRubyRequirePath( StringUtils.underscore( className.trim() ) );

                unit.addToAttachmentList( ServiceMetaData.ATTACHMENTS_KEY, serviceMetaData );
            }
        }
    }

    protected boolean requiresSingleton(Map<String, Object> params) {
        Boolean singleton = params == null ? null : (Boolean) params.remove( "singleton" );
        return singleton != null && singleton.booleanValue();
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.services" );

}
