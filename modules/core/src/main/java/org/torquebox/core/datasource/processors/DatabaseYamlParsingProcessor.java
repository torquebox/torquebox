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

package org.torquebox.core.datasource.processors;

import java.util.Map;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.torquebox.core.datasource.DatabaseMetaData;
import org.torquebox.core.processors.AbstractSplitYamlParsingProcessor;

/**
 * This class is used to read rails database.yml, not a database section
 * of torquebox.yml.
 * 
 */
public class DatabaseYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {
    
    public DatabaseYamlParsingProcessor() {
        setSectionName( "database" );
        setSupportsStandalone( true );
        setStandaloneDeprecated( false );
    }

    @Override
    protected void parse(DeploymentUnit unit, Object data) throws Exception {
        Map<String, Map<String, Object>> file = (Map<String, Map<String, Object>>) data;
        
        for ( String configurationName : file.keySet() ) {
            Map<String, Object> config = file.get( configurationName );
            DatabaseMetaData md = new DatabaseMetaData( configurationName, config );
            unit.addToAttachmentList( DatabaseMetaData.ATTACHMENTS, md );
        }
    }

}
