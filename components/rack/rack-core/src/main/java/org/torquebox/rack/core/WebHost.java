package org.torquebox.rack.core;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Engine;
import org.apache.catalina.Host;
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
                log.info( "Host already registered: " + this.name );
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
        log.debug( "Start virtual host: " + this.name );

        if (this.host == null) {
            log.debug( "Not required: " + this.name );
            return;
        }

        ObjectName serverName = new ObjectName( SERVER_NAME );
        StandardService service = (StandardService) this.mbeanServer.invoke( serverName, "findService", FETCH_ARGS, FETCH_SIGNATURE );
        StandardEngine engine = (StandardEngine) service.getContainer();
        engine.addChild( this.host );
    }

    public void stop() throws Exception {
        log.debug( "Stop virtual host" );
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
    private static final Logger log = Logger.getLogger( WebHost.class );

    private String name;
    private List<String> aliases;
    private MBeanServer mbeanServer;
    private StandardHost host;

}
