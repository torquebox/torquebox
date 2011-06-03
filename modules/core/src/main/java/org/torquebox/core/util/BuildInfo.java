package org.torquebox.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class BuildInfo {

    public BuildInfo() throws IOException {
        InputStream buildInfoStream = getClass().getClassLoader().getResourceAsStream( "org/torquebox/torquebox.properties" );
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
