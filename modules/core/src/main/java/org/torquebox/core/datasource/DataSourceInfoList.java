package org.torquebox.core.datasource;

import java.util.ArrayList;
import java.util.List;

public class DataSourceInfoList {
    
    public static class Info {
        private String name;
        private String jndiName;
        private String adapterName;
        
        public Info(String name, String jndiName, String adapterName) {
            this.name = name;
            this.jndiName = jndiName;
            this.adapterName = adapterName;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getJndiName() {
            return this.jndiName;
        }
        
        public String getAdapterName() {
            return this.adapterName;
        }
    }
    
    public DataSourceInfoList() {
    }
    
    public void addConfiguration(Info info) {
        this.configurations.add( info );
    }
    
    public List<Info> getConfigurations() {
        return this.configurations;
    }
    
    private List<Info> configurations = new ArrayList<Info>();

}
