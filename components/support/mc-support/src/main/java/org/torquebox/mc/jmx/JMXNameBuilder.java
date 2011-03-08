package org.torquebox.mc.jmx;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JMXNameBuilder {

    private String domain;
    private String app;
    private Map<String, String> components = new HashMap<String, String>();

    public JMXNameBuilder(String domain, String app) {
        this.domain = domain;
        this.app = app;
    }

    public JMXNameBuilder with(String key, String value) {
        this.components.put( key, value );
        return this;
    }

    public String name() {
        StringBuilder builder = new StringBuilder();
        builder.append( domain + ":" );
        builder.append( "app=" + this.app );
        Set<String> keys = this.components.keySet();

        for (String key : keys) {
            builder.append( "," );
            String value = this.components.get( key );
            builder.append( key );
            builder.append( "=" );
            builder.append( value );
        }
        return builder.toString();
    }

}
