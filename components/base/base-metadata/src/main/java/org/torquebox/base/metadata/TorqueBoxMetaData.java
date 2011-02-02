package org.torquebox.base.metadata;

import java.util.Map;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

public class TorqueBoxMetaData {
    
    private Map<String,Object> data;
    
    public TorqueBoxMetaData(Map<String,Object> data) {
        this.data = data;
    }
    
    public Object getSection(String name) {
        return this.data.get( name );
    }
    
    @SuppressWarnings("unchecked")
    public String getApplicationRoot() {
        Map<String,String> applicationSection = (Map<String, String>) getSection( "application" );
        if ( applicationSection != null ) {
            return getOneOf( applicationSection, "root", "RAILS_ROOT", "RACK_ROOT" );
        }
        return null;
    }
    
    public VirtualFile getApplicationRootFile() {
        String root = getApplicationRoot();
        
        if ( root == null ) {
            return null;
        }
        
        return VFS.getChild( root );
    }
    
    @SuppressWarnings("unchecked")
    public String getApplicationEnvironment() {
        Map<String,String> applicationSection = (Map<String, String>) getSection( "application" );
        if ( applicationSection != null ) {
            return getOneOf( applicationSection, "env", "RAILS_ENV", "RACK_ENV" );
        }
        return null;
    }
    
    protected String getOneOf(Map<String, String> map, String... keys) {
        for (String each : keys) {
            for (String key : map.keySet()) {
                if (each.equalsIgnoreCase(key)) {
                    return map.get(key);
                }
            }
        }
        return null;
    }

}
