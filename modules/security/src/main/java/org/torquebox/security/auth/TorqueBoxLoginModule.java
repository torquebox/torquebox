package org.torquebox.security.auth;

import java.security.acl.Group;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.jboss.logging.Logger;
import org.jboss.security.auth.spi.UsernamePasswordLoginModule;

/**
 * A simple login module used by torquebox-appname security domains. Usernames
 * and passwords are specified in torquebox.yml
 * 
 * @author lanceball
 * 
 */
public class TorqueBoxLoginModule extends UsernamePasswordLoginModule {

    private Map<String, String> users = new HashMap<String, String>();
    private Group[] roleSets = new Group[0];

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize( subject, callbackHandler, sharedState, options );
        log.info( "Initializing TorqueBoxLoginModule" );
        @SuppressWarnings("unchecked")
        Map<String, String> users = (Map<String, String>) options.get( "credentials" );
        if (users != null) {
            this.users.putAll( users );
        } else {
            log.warn( "TorqueBoxLoginModule: No usernames/passwords provided." );
        }
    }

    @Override
    protected String getUsersPassword() throws LoginException {
        String username = getUsername();
        String password = null;
        if (username != null) {
            password = users.get( username );
        }
        return password;
    }

    @Override
    protected Group[] getRoleSets() throws LoginException {
        return roleSets;
    }

    static final Logger log = Logger.getLogger( "org.torquebox.auth" );
}
