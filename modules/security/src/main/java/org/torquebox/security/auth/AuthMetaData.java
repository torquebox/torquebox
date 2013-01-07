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

package org.torquebox.security.auth;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

public class AuthMetaData {

    public static final AttachmentKey<AttachmentList<AuthMetaData>> ATTACHMENT_KEY = AttachmentKey.createList( AuthMetaData.class );

    private Map<String, TorqueBoxAuthConfig> configs = new HashMap<String, TorqueBoxAuthConfig>();

    public void addAuthentication(String name, Map<String, Object> config) {
        if (name == null) {
            throw new RuntimeException( "Cannot configure unnamed authentication domain." );
        }
        TorqueBoxAuthConfig configItem = new TorqueBoxAuthConfig( name, config );
        configs.put( name, configItem );
    }

    public Collection<TorqueBoxAuthConfig> getConfigurations() {
        return this.configs.values();
    }

    public class TorqueBoxAuthConfig {
        private String name;
        private Map<String, Object> config;

        public TorqueBoxAuthConfig(String name, Map<String, Object> config) {
            this.name = name;
            this.config = config;
        }

        public String getName() {
            return name;
        }

        public String getDomain() {
            return (String) this.config.get( "domain" );
        }

        public void setDomain(String domain) {
            this.config.put( "domain", domain );
        }

        @SuppressWarnings("unchecked")
        public Map<String, String> getCredentials() {
            return (Map<String, String>) this.config.get( "credentials" );
        }
    }

}
