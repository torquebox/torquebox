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

package org.projectodd.polyglot.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class BuildInfo {
    public BuildInfo(String propertiesPath) throws IOException {
        InputStream buildInfoStream = getClass().getClassLoader().getResourceAsStream( propertiesPath );
        Properties props = new Properties();

        props.load( buildInfoStream );

        for (Object each : Collections.list( props.propertyNames() )) {
            String[] parts = ((String) each).split( "\\.", 2 );
            Map<String, String> componentInfo = buildInfo.get( parts[0] );

            if (componentInfo == null) {
                componentInfo = new HashMap<String, String>();
                buildInfo.put( parts[0], componentInfo );
            }

            componentInfo.put( parts[1], props.getProperty( (String) each ) );
        }
    }

    public String get(String component, String key) {
        Map<String, String> componentInfo = getComponentInfo( component );
        if (componentInfo != null) {
            return componentInfo.get( key );
        }
        return null;
    }

    public List<String> getComponentNames() {
        return new ArrayList<String>( this.buildInfo.keySet() );
    }

    public Map<String, String> getComponentInfo(String component) {
        return buildInfo.get( component );
    }

    private Map<String, Map<String, String>> buildInfo = new HashMap<String, Map<String, String>>();

}
