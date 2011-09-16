package org.torquebox.core.datasource;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSourceInfo {
    
    public DataSourceInfo(List<DataSourceMetaData> configs) {
        for ( DataSourceMetaData each : configs ) {
            addConfiguration( each );
        }
    }
    
    protected void addConfiguration(DataSourceMetaData dsMeta) {
        this.info.put( dsMeta.getName(), dsMeta );
    }
    
    public DataSourceMetaData getConfiguration(String name) {
        return info.get(  name  );
    }
    
    public Collection<DataSourceMetaData> getConfigurations() {
        return this.info.values();
    }
    
    private Map<String, DataSourceMetaData> info = new HashMap<String, DataSourceMetaData>();

}
