package org.torquebox.messaging.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.hornetq.api.core.HornetQBuffer;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.server.impl.RemotingServiceImpl;
import org.hornetq.core.server.management.ManagementService;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.spi.core.protocol.ConnectionEntry;
import org.hornetq.spi.core.protocol.ProtocolManager;
import org.hornetq.spi.core.protocol.ProtocolType;
import org.hornetq.spi.core.remoting.Acceptor;
import org.hornetq.spi.core.remoting.BufferHandler;
import org.jboss.logging.Logger;

public class WebSocketsProcessor {

	private static Logger log = Logger.getLogger( WebSocketsProcessor.class );
	
	private JMSServerManager server;
	private String context;
	private int port;

	public String getContext() {
		return context;
	}

	public int getPort() {
		return port;
	}

	@SuppressWarnings("unchecked")
	private Map<ProtocolType, ProtocolManager> getProtocolMap() throws Exception {
		return this.reflectionRetrieve( "protocolMap", Map.class );
	}

	private RemotingServiceImpl getRemotingService() {
		return (RemotingServiceImpl) server.getHornetQServer().getRemotingService();
	}

	public JMSServerManager getServer() {
		return server;
	}

	private ScheduledExecutorService getScheduledThreadPool() throws Exception {
		return this.reflectionRetrieve( "scheduledThreadPool", ScheduledExecutorService.class );
	}

	private ExecutorService getThreadPool() throws Exception {
		return this.reflectionRetrieve( "threadPool", ExecutorService.class );
	}

	/**
	 * Adds web socket support dynamically.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void onInstall() throws Exception {
		log.info( "Installing stomp websocket support for HornetQ." );
		Map<String, Object> params = new HashMap<String, Object>( 10 );
		params.put( "protocol", "stomp_ws" );
		params.put( "port", this.getPort() );

		NettyAcceptorFactory factory = new NettyAcceptorFactory();
		TransportConfiguration info = new TransportConfiguration( NettyAcceptorFactory.class.getName(), params );
		ProtocolManager manager = this.getProtocolMap().get( ProtocolType.STOMP_WS );

		Acceptor acceptor = factory.createAcceptor( info.getParams(), 
				new DelegatingBufferHandler(), 
				manager, 
				this.getRemotingService(),
				getThreadPool(), 
				getScheduledThreadPool() );
		Set<Acceptor> acceptors = (Set<Acceptor>) this.reflectionRetrieve( "acceptors", Set.class );
		acceptors.add( acceptor );

		ManagementService managementService = server.getHornetQServer().getManagementService();
		if (managementService != null) {
			acceptor.setNotificationService( managementService );
			managementService.registerAcceptor( acceptor, info );
		}
		acceptor.start();
	}

	public void onUninstall() {
		log.info( "Uninstalling stomp websocket support for HornetQ." );
	}
	

	/**
	 * This is brittle and ugly but (sadly) necessary, since HornetQ doesn't expose the fields necessary
	 * to create an acceptor dynamically. Maybe some CDI magics will allow me to dispense with 
	 * this nonsense in the future.
	 */	
	@SuppressWarnings("unchecked")
	private <T> T reflectionRetrieve(String fieldName, Class<T> t) throws Exception {
		Field f = RemotingServiceImpl.class.getDeclaredField( fieldName );
		f.setAccessible( true );
		T object = (T) f.get( this.getRemotingService() );
		f.setAccessible( false );
		return object;
	}	

	public void setContext(String context) {
		this.context = context;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setServer(JMSServerManager server) {
		this.server = server;
	}

	private final class DelegatingBufferHandler implements BufferHandler {

		public void bufferReceived(final Object connectionID, final HornetQBuffer buffer) {
			try {
				ConnectionEntry conn = getConnections().get( connectionID );

				if (conn != null) {
					conn.connection.bufferReceived( connectionID, buffer );
				}
			} catch (Exception e) {
				throw new RuntimeException( e );
			}
		}

		@SuppressWarnings("unchecked")
		private Map<Object, ConnectionEntry> getConnections() throws Exception {
			return reflectionRetrieve( "connections", Map.class );
		}

	}

}
