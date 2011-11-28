package org.torquebox.stomp;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.catalina.Host;
import org.apache.catalina.core.StandardHost;
import org.jboss.as.network.NetworkInterfaceBinding;
import org.jboss.as.network.SocketBinding;
import org.jboss.as.network.SocketBindingManager;
import org.jboss.as.server.services.net.SocketBindingManagerService;
import org.jboss.as.web.VirtualHost;
import org.jboss.msc.service.StartContext;
import org.junit.Test;

public class StompEndpointBindingServiceTest {
    
    @Test
    public void testWithoutVirtualHost() throws Exception {
        StompEndpointBindingService service = new StompEndpointBindingService( null, "/"  );
        
        InetAddress address = InetAddress.getByAddress( new byte[] { 10, 42, 42, 2 } );
        Collection<NetworkInterface> networkInterfaces = new ArrayList<NetworkInterface>();
        NetworkInterfaceBinding networkInterfaceBinding = new NetworkInterfaceBinding( networkInterfaces, address );
        SocketBindingManager socketBindingManager = new SocketBindingManagerService( 0 );
        SocketBinding socketBinding = new SocketBinding("stomp", 8675, false, null, 0, networkInterfaceBinding, socketBindingManager );
        service.getSocketBindingInjector().inject( socketBinding );
        
        StartContext context = new MockStartContext();
        service.start( context );
        String binding = service.getValue();
        
        assertNotNull( binding );
        
        assertEquals( "ws://10.42.42.2:8675/", binding );
    }
    
    @Test
    public void testWithStompVirtualHost() throws Exception {
        StompEndpointBindingService service = new StompEndpointBindingService( "tacos.com", "/"  );
        
        InetAddress address = InetAddress.getByAddress( new byte[] { 10, 42, 42, 2 } );
        Collection<NetworkInterface> networkInterfaces = new ArrayList<NetworkInterface>();
        NetworkInterfaceBinding networkInterfaceBinding = new NetworkInterfaceBinding( networkInterfaces, address );
        SocketBindingManager socketBindingManager = new SocketBindingManagerService( 0 );
        SocketBinding socketBinding = new SocketBinding("stomp", 8675, false, null, 0, networkInterfaceBinding, socketBindingManager );
        service.getSocketBindingInjector().inject( socketBinding );
        
        StartContext context = new MockStartContext();
        service.start( context );
        String binding = service.getValue();
        
        assertNotNull( binding );
        
        assertEquals( "ws://tacos.com:8675/", binding );
    }
    
    
    @Test
    public void testWithWebVirtualHost() throws Exception {
        StompEndpointBindingService service = new StompEndpointBindingService( null, "/" );
        
        InetAddress address = InetAddress.getByAddress( new byte[] { 10, 42, 42, 2 } );
        Collection<NetworkInterface> networkInterfaces = new ArrayList<NetworkInterface>();
        NetworkInterfaceBinding networkInterfaceBinding = new NetworkInterfaceBinding( networkInterfaces, address );
        SocketBindingManager socketBindingManager = new SocketBindingManagerService( 0 );
        SocketBinding socketBinding = new SocketBinding("stomp", 8675, false, null, 0, networkInterfaceBinding, socketBindingManager );
        service.getSocketBindingInjector().inject( socketBinding );
        
        Host host = new StandardHost();
        host.setName( "fajitas.com" );
        VirtualHost virtualHost = new VirtualHost( host, false );
        service.getVirtualHostInjector().inject(  virtualHost  );
        
        StartContext context = new MockStartContext();
        service.start( context );
        String binding = service.getValue();
        
        assertNotNull( binding );
        
        assertEquals( "ws://fajitas.com:8675/", binding );
    }
    
    @Test
    public void testWithStompAndWebVirtualHost() throws Exception {
        StompEndpointBindingService service = new StompEndpointBindingService( "tacos.com", "/" );
        
        InetAddress address = InetAddress.getByAddress( new byte[] { 10, 42, 42, 2 } );
        Collection<NetworkInterface> networkInterfaces = new ArrayList<NetworkInterface>();
        NetworkInterfaceBinding networkInterfaceBinding = new NetworkInterfaceBinding( networkInterfaces, address );
        SocketBindingManager socketBindingManager = new SocketBindingManagerService( 0 );
        SocketBinding socketBinding = new SocketBinding("stomp", 8675, false, null, 0, networkInterfaceBinding, socketBindingManager );
        service.getSocketBindingInjector().inject( socketBinding );
        
        Host host = new StandardHost();
        host.setName( "fajitas.com" );
        VirtualHost virtualHost = new VirtualHost( host, false );
        service.getVirtualHostInjector().inject(  virtualHost  );
        
        StartContext context = new MockStartContext();
        service.start( context );
        String binding = service.getValue();
        
        assertNotNull( binding );
        
        assertEquals( "ws://tacos.com:8675/", binding );
    }

}
