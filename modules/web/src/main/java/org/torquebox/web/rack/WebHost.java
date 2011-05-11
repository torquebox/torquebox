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

package org.torquebox.web.rack;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Engine;
import org.apache.catalina.Service;
import org.apache.catalina.core.ServiceMapperListener;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardService;
import org.apache.tomcat.util.http.mapper.Mapper;
import org.jboss.logging.Logger;

public class WebHost {

    public WebHost() {

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public void setMBeanServer(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    public MBeanServer getMBeanServer() {
        return this.mbeanServer;
    }

    protected Service getService() throws Exception {
        ObjectName serverName = new ObjectName( SERVER_NAME );
        return (Service) this.mbeanServer.invoke( serverName, "findService", FETCH_ARGS, FETCH_SIGNATURE );
    }

    protected Engine getEngine() throws Exception {
        return (Engine) getService().getContainer();
    }

    public void create() throws Exception {
        createHost();
    }

    protected void createHost() throws Exception {
        Mapper mapper = getService().getMapper();
        String[] existingHosts = mapper.getHosts();

        for (String host : existingHosts) {
            if (host.equals( this.name )) {
                return;
            }
        }

        this.host = new StandardHost();

        host.setName( this.name );
        for (String alias : this.aliases) {
            host.addAlias( alias );
        }

        host.setConfigClass( "org.jboss.web.tomcat.service.deployers.JBossContextConfig" );
        host.setAppBase( "." );

        mapper.addHost( this.name, this.aliases.toArray( new String[0] ), this.host );
        host.addContainerListener( new ServiceMapperListener( mapper ) );
    }

    public void start() throws Exception {
        if (this.host == null) {
            return;
        }

        ObjectName serverName = new ObjectName( SERVER_NAME );
        StandardService service = (StandardService) this.mbeanServer.invoke( serverName, "findService", FETCH_ARGS, FETCH_SIGNATURE );
        StandardEngine engine = (StandardEngine) service.getContainer();
        engine.addChild( this.host );
    }

    public void stop() throws Exception {
        if (this.host != null) {
            this.host.stop();
        }
    }

    public void destroy() throws Exception {
        if (this.host != null) {
            this.host.destroy();
            this.host = null;
        }
    }

    private static final Object[] FETCH_ARGS = new Object[] { "jboss.web" };
    private static final String[] FETCH_SIGNATURE = new String[] { "java.lang.String" };

    private static final String SERVER_NAME = "jboss.web:type=Server";
    
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( WebHost.class );

    private String name;
    private List<String> aliases;
    private MBeanServer mbeanServer;
    private StandardHost host;

}
