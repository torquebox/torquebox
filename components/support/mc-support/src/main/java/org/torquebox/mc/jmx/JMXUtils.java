package org.torquebox.mc.jmx;


public class JMXUtils {
    
    public static JMXNameBuilder jmxName(String domain, String app) {
        return new JMXNameBuilder( domain, app );
    }

}
