package org.torquebox;

import java.util.List;

import org.jboss.bootstrap.api.descriptor.BootstrapDescriptor;
import org.jboss.bootstrap.api.lifecycle.LifecycleState;
import org.jboss.bootstrap.api.mc.server.MCServer;
import org.jboss.bootstrap.api.mc.server.MCServerFactory;
import org.jboss.logging.Logger;
import org.jboss.reloaded.api.ReloadedDescriptors;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractDeployerTestCase {

	private static Logger log = Logger.getLogger(AbstractDeployerTestCase.class);

	private static MCServer server;

	/**
	 * Creates the server
	 */
	@BeforeClass
	public static void createAndConfigureServer() {
		// Create a server
		final MCServer mcServer = MCServerFactory.createServer();

		// Add the required bootstrap descriptors
		final List<BootstrapDescriptor> descriptors = mcServer.getConfiguration().getBootstrapDescriptors();
		descriptors.add(ReloadedDescriptors.getClassLoadingDescriptor());
		descriptors.add(ReloadedDescriptors.getVdfDescriptor());

		//log.info("Using bootstrap descriptors:" + descriptors);

		// Set
		server = mcServer;
	}

	@Before
	public void startServer() throws Exception {
		server.start();
		
		long start = System.currentTimeMillis();
		
		while ( server.getState() == LifecycleState.STARTING ) {
			Thread.sleep( 50 );
		}
		
		long elapsed = System.currentTimeMillis() - start;
		
		log.info( "Server started in " + elapsed + "ms" );
	}

	@After
	public void stopServer() throws Exception {
		if (server != null && server.getState().equals(LifecycleState.STARTED)) {
			server.stop();
		}

	}

}
