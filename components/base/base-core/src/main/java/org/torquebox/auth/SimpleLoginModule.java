package org.torquebox.auth;

import java.util.Map;
import java.util.Properties;

import org.jboss.security.auth.spi.UsersRolesLoginModule;

public class SimpleLoginModule extends UsersRolesLoginModule {
    private Properties users = new Properties();
    private Properties roles = new Properties();
    
    @Override
    protected Properties createUsers(Map<String,?> options) { 
    	System.err.println("I'm suspposed to be loading a users properties file");
        return users;
    }

    @Override
    protected Properties createRoles(Map<String,?> options) {
    	System.err.println("I'm suspposed to be loading a roles properties file");
        return roles;
    }

    @Override
    public boolean login() {
      throw new RuntimeException("EAT ME");
    }
    
}
