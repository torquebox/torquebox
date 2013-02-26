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
        this.addValidOptions( new String[] { "credentials" } );
        super.initialize( subject, callbackHandler, sharedState, options );
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

    static final Logger log = Logger.getLogger( "org.torquebox.security" );
}
