package org.torquebox;

import java.util.List;

import org.jboss.bootstrap.api.descriptor.BootstrapDescriptor;
import org.jboss.bootstrap.api.mc.server.MCServer;
import org.jboss.bootstrap.api.mc.server.MCServerFactory;
import org.jboss.reloaded.api.ReloadedDescriptors;
import org.junit.BeforeClass;

public abstract class AbstractDeployerTestCase {
	
	   /**
	    * Creates the server
	    */
	   @BeforeClass
	   public static void createAndConfigureServer()
	   {
	      // Create a server
	      final MCServer mcServer = MCServerFactory.createServer();

	      // Add the required bootstrap descriptors
	      final List<BootstrapDescriptor> descriptors = mcServer.getConfiguration().getBootstrapDescriptors();
	      descriptors.add(ReloadedDescriptors.getClassLoadingDescriptor());
	      descriptors.add(ReloadedDescriptors.getVdfDescriptor());

	      //log.info("Using bootstrap descriptors:" + descriptors);

	      // Set
	      //server = mcServer;
	   }


}
