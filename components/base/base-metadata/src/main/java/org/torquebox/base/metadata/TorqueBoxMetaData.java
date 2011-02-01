package org.torquebox.base.metadata;

import java.util.Map;

public class TorqueBoxMetaData {
    
    private Map<String,Object> data;
    
    public TorqueBoxMetaData(Map<String,Object> data) {
        this.data = data;
    }
    
    public Object getSection(String name) {
        return this.data.get( name );
    }

}
