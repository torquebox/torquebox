package org.torquebox.auth;

import java.util.Map;
import java.util.Properties;

import org.jboss.security.auth.spi.UsersRolesLoginModule;

public class SimpleLoginModule extends UsersRolesLoginModule {
    private Properties users = new Properties();
    private Properties roles = new Properties();
    
    @Override
    protected Properties createUsers(Map<String,?> options) { 
        return users;
    }

    @Override
    protected Properties createRoles(Map<String,?> options) {
        return roles;
    }
    
    public void setUsersMap(Map<String,String> users) {
        this.users.clear();
        this.users.putAll(users);
    }
    
    public void setRolesMap(Map<String,String> roles) {
        this.roles.clear();
        this.roles.putAll(roles);
    }
}
