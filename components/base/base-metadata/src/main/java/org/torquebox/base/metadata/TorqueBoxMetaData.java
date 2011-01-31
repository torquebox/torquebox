package org.torquebox.base.metadata;

import java.util.Map;

public class TorqueBoxMetaData {
    
    private Map<String,Map<String,?>> data;
    
    public TorqueBoxMetaData(Map<String,Map<String,?>> data) {
        this.data = data;
    }
    
    public Map<String,?> getSection(String name) {
        Map<String,?> section = this.data.get( name );
        return section;
    }

}
