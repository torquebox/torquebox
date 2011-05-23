package org.torquebox.messaging.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.hornetq.api.core.HornetQBuffer;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.remoting.impl.netty.NettyAcceptor;
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

public class StompServer {

	private static final Logger log = Logger.getLogger( StompServer.class );

	private int port;

	private RemotingServiceImpl remotingService;

	private JMSServerManager server;

	@SuppressWarnings("unchecked")
	public void onInstall() throws Exception {
		log.info( "Starting HornetQ STOMP acceptor on port " + port );
		remotingService = (RemotingServiceImpl) server.getHornetQServer().getRemotingService();

		Map<String, Object> configuration = new HashMap<String, Object>( 2 );
		configuration.put( "protocol", "stomp" );
		configuration.put( "port", port );

		Map<String, ProtocolManager> protocolMap = this.reflectionRetrieve( remotingService, "protocolMap", Map.class );
		ProtocolManager stompProtocolManager = protocolMap.get( ProtocolType.STOMP );
		DelegatingBufferHandler decoder = new DelegatingBufferHandler();
		ExecutorService threadPool = this.reflectionRetrieve( remotingService, "threadPool", ExecutorService.class );
		ScheduledExecutorService scheduledThreadPool = this.reflectionRetrieve( remotingService, "scheduledThreadPool",
				ScheduledExecutorService.class );
		NettyAcceptorFactory factory = new NettyAcceptorFactory();
		TransportConfiguration info = new TransportConfiguration( NettyAcceptor.class.getName(), configuration );
		Acceptor acceptor = factory.createAcceptor( info.getParams(), decoder, stompProtocolManager, remotingService,
				threadPool, scheduledThreadPool );
		Set<Acceptor> acceptors = this.reflectionRetrieve( remotingService, "acceptors", Set.class );
		acceptors.add( acceptor );

		ManagementService managementService = server.getHornetQServer().getManagementService();
		if (managementService != null) {
			acceptor.setNotificationService( managementService );
			managementService.registerAcceptor( acceptor, info );
		}
		acceptor.start();

		log.info( "HornetQ STOMP acceptor started." );
	}

	public void onUninstall() {
		log.info( "Stopping HornetQ STOMP acceptor." );
		// TODO: do we need any cleanup here?
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public void setServer(JMSServerManager server) {
		this.server = server;
	}

	@SuppressWarnings("unchecked")
	private <T, F> T reflectionRetrieve(F source, String property, Class<T> type) {
		try {
			Field f = source.getClass().getDeclaredField( property );
			f.setAccessible( true );
			T value = (T) f.get( source );
			f.setAccessible( false );
			return value;
		} catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

	private final class DelegatingBufferHandler implements BufferHandler {

		@SuppressWarnings("unchecked")
		public void bufferReceived(final Object connectionID, final HornetQBuffer buffer) {
			Map<Object, ConnectionEntry> connections = reflectionRetrieve( remotingService, "connections", Map.class );
			ConnectionEntry conn = connections.get( connectionID );

			if (conn != null) {
				conn.connection.bufferReceived( connectionID, buffer );
			}
		}
	}

}
