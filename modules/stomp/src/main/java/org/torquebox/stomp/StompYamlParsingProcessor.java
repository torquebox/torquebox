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

package org.torquebox.stomp;

import java.util.Map;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.Logger;
import org.torquebox.core.AbstractSplitYamlParsingProcessor;
import org.torquebox.core.util.StringUtils;

/**
 * Creates ScheduledJobMetaData instances from jobs.yml
 */
public class StompYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public StompYamlParsingProcessor() {
        setSectionName( "stomp" );
    }

    @SuppressWarnings("unchecked")
    public void parse(DeploymentUnit unit, Object dataObject) throws DeploymentUnitProcessingException {
        Map<String, Map<String, ?>> data = (Map<String, Map<String, ?>>) dataObject;

        log.info( "Parsing: " + data );
        
        for ( String name : data.keySet() ) {
            Map<String, ?> config = data.get( name );
            
            String destinationPattern = (String) config.get( "route" );
            String rubyClassName = (String) config.get( "class" );
            Map<String, String> stompletConfig = (Map<String, String>) config.get( "config" );
            
            RubyStompletMetaData metaData = new RubyStompletMetaData( name );
            metaData.setDestinationPattern( destinationPattern );
            metaData.setRubyClassName( rubyClassName );
            metaData.setRubyRequirePath( StringUtils.underscore( rubyClassName.trim() ) );
            metaData.setStompletConfig( stompletConfig );
            
            unit.addToAttachmentList( RubyStompletMetaData.ATTACHMENTS_KEY, metaData );
        }

    }

    private static final Logger log = Logger.getLogger( "org.torquebox.stomp" );
}
