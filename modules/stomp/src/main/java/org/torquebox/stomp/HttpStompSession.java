/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

package org.torquebox.stomp;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.catalina.Session;
import org.projectodd.stilts.stomp.spi.StompSession;

public class HttpStompSession implements StompSession {

    public HttpStompSession(HttpSession webSession) {
        this.webSession = webSession;
    }
    
    @Override
    public String getId() {
        return this.webSession.getId();
    }

    @Override
    public List<String> getAttributeNames() {
        List<String> names = new ArrayList<String>();
        
        Enumeration<String> webNames = this.webSession.getAttributeNames();
        
        while ( webNames.hasMoreElements() ) {
            names.add( webNames.nextElement() );
        }
        
        return names;
    }

    @Override
    public Object getAttribute(String name) {
        return this.webSession.getAttribute( name );
    }

    @Override
    public void setAttribute(String name, Object value) {
        this.webSession.setAttribute( name, value );
    }

    @Override
    public void removeAttribute(String name) {
        this.webSession.removeAttribute( name );
    }
    
    public void access() {
        ((Session)this.webSession).access();
    }
    
    public void endAccess() {
        ((Session)this.webSession).endAccess();
    }

    private HttpSession webSession;

}
