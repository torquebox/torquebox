package org.torquebox.auth;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

public class AuthMetaData {
	
    public static final AttachmentKey<AttachmentList<AuthMetaData>> ATTACHMENT_KEY = AttachmentKey.createList(AuthMetaData.class);

    private Map<String, Config> configs = new HashMap<String, Config>();

    public void addAuthentication(String name, String domain) {
        Config configItem = new Config();
        configItem.setName(name);
        configItem.setDomain(domain);
        configs.put(name, configItem);
    }

    public Collection<Config> getConfigurations() {
        return this.configs.values();
    }

    public class Config {
        private String name;
        private String domain;

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
    }

}
