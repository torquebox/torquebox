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

package org.torquebox.messaging.core;

import org.hornetq.jms.server.JMSServerManager;
import org.jboss.logging.Logger;

public abstract class AbstractManagedDestination {

    protected Logger log;

    private JMSServerManager server;
    private String name;

    public AbstractManagedDestination() {
        log = Logger.getLogger( getClass() );
    }

    public void setServer(JMSServerManager server) {
        this.server = server;
    }

    public JMSServerManager getServer() {
        return this.server;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract void create() throws Exception;

    public abstract void destroy() throws Exception;
}
