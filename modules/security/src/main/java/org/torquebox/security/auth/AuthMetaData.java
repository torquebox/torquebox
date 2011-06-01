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
