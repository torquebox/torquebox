package org.torquebox;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.bootstrap.api.descriptor.BootstrapDescriptor;
import org.jboss.bootstrap.api.lifecycle.LifecycleState;
import org.jboss.bootstrap.api.mc.server.MCServer;
import org.jboss.bootstrap.api.mc.server.MCServerFactory;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.reloaded.api.ReloadedDescriptors;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileAssembly;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractDeployerTestCase {

	private static Logger baseLog = Logger.getLogger(AbstractDeployerTestCase.class);

	/**
	 * MC bean name of the {@link MainDeployer}
	 */
	protected static final String MC_MAIN_DEPLOYER_NAME = "MainDeployer";

	private static MCServer server;

	protected Logger log;
	
	public AbstractDeployerTestCase() {
		this.log = Logger.getLogger( getClass() );
	}

	/**
	 * Creates the server
	 */
	@BeforeClass
	public static void createAndConfigureServer() {
		final MCServer mcServer = MCServerFactory.createServer();

		final List<BootstrapDescriptor> descriptors = mcServer.getConfiguration().getBootstrapDescriptors();
		descriptors.add(ReloadedDescriptors.getClassLoadingDescriptor());
		descriptors.add(ReloadedDescriptors.getVdfDescriptor());

		server = mcServer;
	}

	private Closeable mount;

	@Before
	public void startServer() throws Exception {
		server.start();

		long start = System.currentTimeMillis();

		long elapsed = System.currentTimeMillis() - start;

		baseLog.info("Server started in " + elapsed + "ms");
	}

	@After
	public void stopServer() throws Exception {
		if ( mount != null ) {
			mount.close();
			mount = null;
		}
		if (server != null && server.getState().equals(LifecycleState.STARTED)) {
			server.stop();
		}

	}

	protected void addDeployer(Deployer deployer) throws Throwable {

		KernelController controller = server.getKernel().getController();
		String deployerName = deployer.getClass().getSimpleName();
		BeanMetaDataBuilder bmdb = BeanMetaDataBuilder.createBuilder(deployerName, deployer.getClass().getName());
		controller.install(bmdb.getBeanMetaData(), deployer);

	}
	
	protected String addDeployment(URL url, String name) throws IOException, URISyntaxException, DeploymentException {
		
		File tmpRoot = File.createTempFile(getClass().getName(), ".tmp" );
		tmpRoot.deleteOnExit();
		
		VirtualFileAssembly assembly = new VirtualFileAssembly();
		assembly.add( name, VFS.getChild( url ) );
		
		VirtualFile mountPoint = VFS.getChild( tmpRoot.getAbsolutePath() );
		mount = VFS.mountAssembly(assembly, mountPoint);
		
		return addDeployment( mountPoint.getChild( name ) );
	}

	protected String addDeployment(File file) throws DeploymentException {
		VirtualFile virtualFile = VFS.getChild( file.getAbsolutePath() );
		return addDeployment( virtualFile );
	}
	
	protected String addDeployment(URL url) throws DeploymentException, URISyntaxException {
		VirtualFile file = VFS.getChild( url );
		return addDeployment( file );
	}
	
	protected String addDeployment(VirtualFile file) throws DeploymentException {
		VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(file);
		return addDeployment( deployment );
	}

	protected String addDeployment(Deployment deployment) throws DeploymentException {
		MainDeployer mainDeployer = getMainDeployer();
		mainDeployer.addDeployment(deployment);
		return deployment.getName();
	}

	protected KernelController getKernelController() {
		return server.getKernel().getController();
	}

	protected MainDeployer getMainDeployer() {
		return (MainDeployer) getKernelController().getInstalledContext(MC_MAIN_DEPLOYER_NAME).getTarget();
	}
	
	protected void processDeployments() throws DeploymentException {
		processDeployments(false);
	}
	
	protected void processDeployments(boolean checkComplete) throws DeploymentException {
		getMainDeployer().process();
		if ( checkComplete ) {
			getMainDeployer().checkComplete();
		}
	}
	
	protected ManagedDeployment getDeployment(String name) throws DeploymentException {
		return getMainDeployer().getManagedDeployment( name );
	}
	
	protected DeploymentUnit getDeploymentUnit(String name) {
		return ((MainDeployerImpl)getMainDeployer()).getDeploymentUnit( name );
	}
	
}