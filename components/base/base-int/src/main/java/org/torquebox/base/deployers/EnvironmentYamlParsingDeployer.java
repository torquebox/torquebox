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

package org.torquebox.base.deployers;

import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;

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
public class EnvironmentYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {

    public EnvironmentYamlParsingDeployer() {
        setSectionName( "environment" );
        setSupportsStandalone( false );
        addInput( RubyApplicationMetaData.class );
        addOutput( RubyApplicationMetaData.class );
    }

    @SuppressWarnings("unchecked")
    public void parse(VFSDeploymentUnit unit, Object dataObj) throws Exception {
        log.debug( "Deploying ruby environment: " + unit );

        RubyApplicationMetaData appMetaData = unit.getAttachment( RubyApplicationMetaData.class );

        if (appMetaData == null) {
            log.debug( "Configuring ruby environment: " + unit );
            appMetaData = new RubyApplicationMetaData();
            appMetaData.setApplicationName( unit.getSimpleName() );
            unit.addAttachment( RubyApplicationMetaData.class, appMetaData );
        } else {
            log.debug( "Configuring pre-existing ruby environment: " + unit + "\n  " + appMetaData );
        }
        
        Map<String, String> env = (Map<String, String>) dataObj;

        Map<String, String> appEnv = appMetaData.getEnvironmentVariables();

        if (appEnv == null) {
            appEnv = new HashMap<String, String>();
            appMetaData.setEnvironmentVariables( appEnv );
        }
        
        appEnv.putAll( env );
        
        log.debug( "Configured ruby application: " + unit + "\n  " + appMetaData );

    }
}
