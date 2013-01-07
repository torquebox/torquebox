/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.core.datasource;

import java.util.ArrayList;
import java.util.List;

import org.jboss.msc.service.ServiceName;
import org.torquebox.core.datasource.db.Adapter;

public class DataSourceInfoList {
    
    public static class Info {
        public static final Info DISABLED = new Info( null, null, null, null, null);
        
        private String name;
        private String jndiName;
        private String adapterName;
        private ServiceName serviceName;
        private Adapter adapter;
        
        public Info(String name, String jndiName, String adapterName, ServiceName serviceName, Adapter adapter) {
            this.name = name;
            this.jndiName = jndiName;
            this.adapterName = adapterName;
            this.serviceName = serviceName;
            this.adapter = adapter;
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
        
        public Adapter getAdapter() {
            return this.adapter;
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
