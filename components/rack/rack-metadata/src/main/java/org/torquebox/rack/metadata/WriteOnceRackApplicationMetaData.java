package org.torquebox.rack.metadata;

import java.util.Map;
import org.jboss.vfs.VirtualFile;

/**
 * This is a write-once bean, in which setters only take effect if
 * their corresponding getter returns null.  Setters taking a Map only
 * write those entries whose keys aren't already in the Map.
 *
 * This reflects the idea that higher-priority deployers, e.g. those
 * reading external descriptors, manipulate the meta data *before*
 * lower-priority ones, e.g. those reading internal descriptors.
 *
 * This would be SO MUCH EASIER (and smaller, and more robust,
 * resilient to changes in the baseclass) in a dynamic language!
 */
public class WriteOnceRackApplicationMetaData extends RackApplicationMetaData {
    
    public void setRackRoot(VirtualFile rackRoot) {
        if (null == getRackRoot()) super.setRackRoot( rackRoot );
    }
    
    public void setRackEnv(String rackEnv) {
        if (null == getRackEnv()) super.setRackEnv( rackEnv );
    }
    
    public void setRackUpScript(String rackUpScript) {
        if (null == getRackUpScript()) super.setRackUpScript( rackUpScript );
    }

    public void setRackUpScriptLocation(VirtualFile rackUpScriptLocation) {
        if (null == getRackUpScriptLocation()) super.setRackUpScriptLocation( rackUpScriptLocation );
    }
    
    public void setContextPath(String contextPath) {
        if (null == getContextPath()) super.setContextPath( contextPath );
    }
    
    public void setStaticPathPrefix(String staticPathPrefix) {
        if (null == getStaticPathPrefix()) super.setStaticPathPrefix( staticPathPrefix );
    }
    
    public void setRubyRuntimePoolName(String rubyRuntimePoolName) {
        if (null == getRubyRuntimePoolName()) super.setRubyRuntimePoolName( rubyRuntimePoolName );
    }
    
    public void setRackApplicationFactoryName(String rackApplicationFactoryName) {
        if (null == getRackApplicationFactoryName()) super.setRackApplicationFactoryName( rackApplicationFactoryName );
    }
    
    public void setRackApplicationPoolName(String rackApplicationPoolName) {
        if (null == getRackApplicationPoolName()) super.setRackApplicationPoolName( rackApplicationPoolName );
    }
    
    public void setEnvironmentVariables(Map<String,String> environment) {
        if (null == getEnvironmentVariables()) { 
            super.setEnvironmentVariables( environment );
        } else {
            Map<String,String> current = getEnvironmentVariables();
            for (String key: environment.keySet()) {
                if ( ! current.containsKey(key) ) {
                    current.put( key, environment.get(key) );
                }
            }
        }
    }

}
