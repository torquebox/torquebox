package org.torquebox.core.datasource;

import java.util.ArrayList;
import java.util.List;

import org.jboss.msc.service.ServiceName;

public class DataSourceInfoList {
    
    public static class Info {
        private String name;
        private String jndiName;
        private String adapterName;
        private ServiceName serviceName;
        
        public Info(String name, String jndiName, String adapterName, ServiceName serviceName) {
            this.name = name;
            this.jndiName = jndiName;
            this.adapterName = adapterName;
            this.serviceName = serviceName;
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
        
        public ServiceName getServiceName() {
            return this.serviceName;
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
