package org.torquebox.auth;

import java.util.Map;
import java.util.Properties;

import org.jboss.security.auth.spi.UsersRolesLoginModule;

public class SimpleLoginModule extends UsersRolesLoginModule {
    
    @Override
    protected Properties createUsers(Map<String,?> options) { 
        return extractPairs(options, "usersMap");
    }

    @Override
    protected Properties createRoles(Map<String,?> options) {
        return extractPairs(options, "rolesMap");
    }

    private Properties extractPairs(Map<String, ?> options, String optionKey) {
        Properties properties = new Properties();
        @SuppressWarnings("unchecked")
        Map<String,String> propertyMap = (Map<String, String>) options.get(optionKey);
        for (String propertyKey: propertyMap.keySet()) {
            properties.put(propertyKey, propertyMap.get(propertyKey));
        }
        return properties;
    }
}
