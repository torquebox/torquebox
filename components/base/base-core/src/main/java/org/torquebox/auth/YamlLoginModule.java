package org.torquebox.auth;

import java.security.acl.Group;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.jboss.security.auth.spi.UsernamePasswordLoginModule;

public class YamlLoginModule extends UsernamePasswordLoginModule {
    
    private Properties users;
    private Properties roles;

    public YamlLoginModule() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String getUsersPassword() throws LoginException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Group[] getRoleSets() throws LoginException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean login() throws LoginException {
        // TODO Auto-generated method stub
        return super.login();
    }
    
    public void setUsers(Properties users) {
        this.users = users;
    }
    
    public void setRoles(Properties roles) {
        this.roles = roles;
    }

}
