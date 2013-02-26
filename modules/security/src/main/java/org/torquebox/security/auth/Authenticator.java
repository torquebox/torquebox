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

import java.security.Principal;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SecurityContext;
import org.picketbox.factories.SecurityFactory;

public class Authenticator implements Service<Authenticator> {

    public void setAuthDomain(String domain) {
        this.authDomain = domain;
    }

    public String getAuthDomain() {
        return this.authDomain;
    }

    public boolean authenticate(String name, String pass) {
        log.debug( "Authenticating " + name );
        Principal principal = getPrincipal( name );
        log.debug( "Found principal: " + principal.getName() );
        Object credential = (pass == null ? null : new String( pass ));
        boolean isValid = this.authenticationManager.isValid( principal, credential );
        log.debug("Auth manager says this login " + (isValid ? "is" : "is not") + " valid.");
        return isValid;
    }

    public AuthenticationManager getAuthenticationManager() {
        return this.authenticationManager;
    }

    public SecurityContext getSecurityContext() {
        return this.securityContext;
    }

    @Override
    public Authenticator getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        context.asynchronous();
        context.execute( new Runnable() {
            public void run() {
                try {
                    Authenticator.this.start();
                    context.complete();
                } catch (Exception e) {
                    context.failed( new StartException( e ) );
                }
            }
        } );
    }

    protected void start() throws Exception {
        // TODO what's up with this classloader?
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
//            Thread.currentThread().setContextClassLoader( Authenticator.class.getClassLoader() );
            this.securityContext = SecurityFactory.establishSecurityContext( this.getAuthDomain() );
            this.authenticationManager = securityContext.getAuthenticationManager();
            log.debug( "Found authentication manager for security context [" + this.securityContext.getSecurityDomain() + "]. " + this.authenticationManager.getSecurityDomain() );
        } catch (Exception e) {
            log.error( "Unable to initialize TorqueBox security subsystem: " + e.getLocalizedMessage() );
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader( originalClassLoader );
        }

    }

    @Override
    public void stop(StopContext context) {
        // TODO destroy/release authenticationManager and securityContext
    }

    private Principal getPrincipal(final String name) {
        return new Principal() {
            public String getName() {
                return name;
            }
        };
    }
    
    public static final String TORQUEBOX_AUTH_DOMAIN = "torquebox";

    private String authDomain;
    private SecurityContext securityContext;
    private AuthenticationManager authenticationManager;
    private static final Logger log = Logger.getLogger( "org.torquebox.security" );
}
