package org.torquebox.mc.jmx;

import java.util.Map;

public class JMXUtils {
    
    public static JMXNameBuilder jmxName(String domain, String app) {
        return new JMXNameBuilder( domain, app );
    }

}
