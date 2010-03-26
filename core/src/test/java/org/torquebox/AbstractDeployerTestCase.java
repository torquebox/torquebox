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

		log.info("Using bootstrap descriptors:" + descriptors);
		System.err.println("Create server");

		// Set
		server = mcServer;
	}

	@Before
	public void startServer() throws Exception {
		log.info("Start server");
		System.err.println("Start server");
		server.start();
	}

	@After
	public void stopServer() throws Exception {
		log.info("Stop server");
		System.err.println("Stop server");
		if (server != null && server.getState().equals(LifecycleState.STARTED)) {
			server.stop();
		}

	}

}
