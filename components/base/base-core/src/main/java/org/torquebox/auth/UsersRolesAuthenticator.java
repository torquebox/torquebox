/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

package org.torquebox.auth;

import java.security.Principal;

import org.jboss.security.AuthenticationManager;
import org.jboss.security.SecurityContext;
import org.picketbox.factories.SecurityFactory;

/**
 * Provides JBoss file-based authentication
 * auth bits to ruby apps.
 *
 * @author Lance Ball <lball@redhat.com>
 */
public class UsersRolesAuthenticator
{
	private String authDomain;
	
	public void setAuthDomain(String domain) {
		this.authDomain = domain;
	}
	
	public String getAuthDomain() {
		return this.authDomain;
	}
	
	public boolean authenticate(String name, String pass) {
         SecurityContext securityContext = SecurityFactory.establishSecurityContext(this.getAuthDomain()); 
         AuthenticationManager am = securityContext.getAuthenticationManager(); 
         Principal principal = getPrincipal(name);
         Object credential = new String(pass);
         return am.isValid(principal, credential); 
    }
	
	private Principal getPrincipal(final String name)
	{
	     return new Principal()
	     {
	         public String getName()
	         {
	            return name;
	         }
	     };
	}

}
