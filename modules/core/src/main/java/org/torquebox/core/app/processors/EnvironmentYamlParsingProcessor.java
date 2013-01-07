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

import java.util.HashMap;
import java.util.Map;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.processors.AbstractSplitYamlParsingProcessor;

/**
 * <pre>
 * Stage: PARSE
 *    In: web.yml
 *   Out: RubyApplicationMetaData
 * </pre>
 * 
 * Internal deployment descriptor for setting vhosts, web context, and static
 * content dir
 * 
 */
public class EnvironmentYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public EnvironmentYamlParsingProcessor() {
        setSectionName( "environment" );
        setSupportsStandalone( false );
    }

    @SuppressWarnings("unchecked")
    public void parse(DeploymentUnit unit, Object dataObj) throws Exception {
        
        RubyAppMetaData appMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );

        if (appMetaData == null) {
            appMetaData = new RubyAppMetaData( unit.getName() );
            appMetaData.attachTo( unit );
        }

        Map<String, Object> envData = (Map<String, Object>) dataObj;
        Map<String, String> env = new HashMap<String, String>();

        for (String key : envData.keySet()) {
            env.put( key, envData.get( key ).toString() );
        }

        Map<String, String> appEnv = appMetaData.getEnvironmentVariables();

        if (appEnv == null) {
            appEnv = new HashMap<String, String>();
            appMetaData.setEnvironmentVariables( appEnv );
        }

        appEnv.putAll( env );
    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.core.app.env" );

}
