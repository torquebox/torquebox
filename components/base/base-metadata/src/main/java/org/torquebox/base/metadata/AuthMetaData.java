/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

package org.torquebox.base.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AuthMetaData {

    private Map<String, Config> configs = new HashMap<String, Config>();

    public void addAuthentication(String name, String domain, String strategy, Map<String,String> users, Map<String,String> roles) {
        Config configItem = new Config();
        configItem.setName(name);
        configItem.setDomain(domain);
        configItem.setStrategy(strategy);
        configItem.setUsers(users);
        configItem.setRoles(roles);
        configs.put(name, configItem);
    }
    
    public void addAuthentication(String name, String domain, String strategy) {
        this.addAuthentication(name, domain, strategy, new HashMap<String,String>(), new HashMap<String,String>());
    }

    public Collection<Config> getConfigurations() {
        return this.configs.values();
    }

    public class Config {
        private String name;
        private String domain;
        private String strategy;
        private Map<String,String> users;
        private Map<String,String> roles;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getDomain() {
            return domain;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public String getStrategy() {
            return strategy;
        }

        public void setUsers(Map<String,String> users) {
            this.users = users;
        }

        public Map<String,String> getUsers() {
            return users;
        }

        public void setRoles(Map<String,String> roles) {
            this.roles = roles;
        }

        public Map<String,String> getRoles() {
            return roles;
        }
    }

}
