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

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.apache.catalina.Manager;
import org.projectodd.stilts.conduit.spi.StompSessionManager;
import org.projectodd.stilts.conduit.stomp.SimpleStompSessionManager;
import org.projectodd.stilts.stomp.StompException;
import org.projectodd.stilts.stomp.spi.StompSession;

public class HttpStompSessionManager extends SimpleStompSessionManager implements StompSessionManager {

    public HttpStompSessionManager(Manager webSessionManager) {
        this.webSessionManager = webSessionManager;
    }

    @Override
    public StompSession findSession(String sessionId) throws StompException {
        try {
            HttpSession webSession = (HttpSession) webSessionManager.findSession( sessionId );
            if (webSession != null) {
                return new HttpStompSession( webSession );
            }
            return super.findSession(sessionId);
        } catch (IOException e) {
            throw new StompException( e );
        }
    }

    private Manager webSessionManager;

}
