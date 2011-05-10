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

package org.torquebox.core.app;

import java.util.Map;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.torquebox.core.AbstractSplitYamlParsingProcessor;


/**
 * <pre>
 * Stage: PARSE
 *    In: web.yml
 *   Out: RackApplicationMetaData
 * </pre>
 * 
 * Internal deployment descriptor for setting vhosts, web context, and static
 * content dir
 * 
 */
public class ApplicationYamlParsingDeployer extends AbstractSplitYamlParsingProcessor {

    public ApplicationYamlParsingDeployer() {
        setSectionName( "application" );
        setSupportsStandalone( false );
    }

    @SuppressWarnings("unchecked")
    public void parse(DeploymentUnit unit, Object dataObj) throws Exception {
        Map<String, String> app = (Map<String, String>) dataObj;

        RubyApplicationMetaData appMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );

        if (appMetaData == null) {
            appMetaData = new RubyApplicationMetaData();
            appMetaData.setApplicationName( unit.getName() );
            unit.putAttachment( RubyApplicationMetaData.ATTACHMENT_KEY, appMetaData );
        }

        if (appMetaData.getRoot() == null) {
            String root = getOneOf( app, "root", "RAILS_ROOT", "RACK_ROOT" );

            if (root != null && !root.trim().equals( "" )) {
                appMetaData.setRoot( root.trim() );
            }
        }
        
        if (appMetaData.getEnvironmentName() == null) {
            String env = getOneOf( app, "env", "RAILS_ENV", "RACK_ENV" );

            if (env != null && !env.trim().equals( "" )) {
                appMetaData.setEnvironmentName( env.trim() );
            }

        }
    }
}
