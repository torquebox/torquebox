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

package org.torquebox.test.mc.vdf;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.jboss.beans.metadata.spi.BeanMetaData;
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
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.reloaded.api.ReloadedDescriptors;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileAssembly;
import org.junit.After;
import org.junit.Before;
import org.torquebox.test.mc.vfs.AbstractVFSTestCase;

public abstract class AbstractDeployerTestCase extends AbstractVFSTestCase {

    /**
     * MC bean name of the {@link MainDeployer}
     */
    protected static final String MC_MAIN_DEPLOYER_NAME = "MainDeployer";

    private MCServer server;

    protected Logger log;

    public AbstractDeployerTestCase() {
        this.log = Logger.getLogger( getClass() );
    }

    private Closeable mount;

    @Before
    public void startServer() throws Exception {
        this.server = MCServerFactory.createServer();

        final List<BootstrapDescriptor> descriptors = this.server.getConfiguration().getBootstrapDescriptors();
        descriptors.add( ReloadedDescriptors.getClassLoadingDescriptor() );
        descriptors.add( ReloadedDescriptors.getVdfDescriptor() );

        long start = System.currentTimeMillis();
        this.server.start();
        long elapsed = System.currentTimeMillis() - start;

        log.info( "Server started in " + elapsed + "ms" );
    }

    @After
    public void stopServer() throws Exception {
        if (this.mount != null) {
            this.mount.close();
            this.mount = null;
        }
        if (this.server != null && this.server.getState().equals( LifecycleState.STARTED )) {
            this.server.stop();
        }

    }

    protected void addDeployer(Deployer deployer) throws Throwable {
        String deployerName = deployer.getClass().getSimpleName();
        addDeployer( deployer, deployerName );
    }

    protected void addDeployer(Deployer deployer, String deployerName) throws Throwable {
        KernelController controller = this.server.getKernel().getController();
        BeanMetaDataBuilder bmdb = BeanMetaDataBuilder.createBuilder( deployerName, deployer.getClass().getName() );
        controller.install( bmdb.getBeanMetaData(), deployer );
    }

    protected void addStructureDeployer(StructureDeployer deployer) throws Throwable {
        String deployerName = deployer.getClass().getSimpleName();
        addStructureDeployer( deployer, deployerName );
    }

    protected void addStructureDeployer(StructureDeployer deployer, String deployerName) throws Throwable {
        KernelController controller = this.server.getKernel().getController();
        BeanMetaDataBuilder bmdb = BeanMetaDataBuilder.createBuilder( deployerName, deployer.getClass().getName() );
        controller.install( bmdb.getBeanMetaData(), deployer );
    }

    protected String addDeployment(URL url, String name) throws IOException, URISyntaxException, DeploymentException {

        File tmpRoot = File.createTempFile( getClass().getName(), ".tmp" );
        tmpRoot.deleteOnExit();

        VirtualFileAssembly assembly = new VirtualFileAssembly();
        assembly.add( name, VFS.getChild( url.toURI() ) );

        VirtualFile mountPoint = VFS.getChild( tmpRoot.getAbsolutePath() );
        this.mount = VFS.mountAssembly( assembly, mountPoint );

        return addDeployment( mountPoint.getChild( name ) );
    }

    protected String addDeployment(File file) throws DeploymentException {
        VirtualFile virtualFile = VFS.getChild( file.getAbsolutePath() );
        return addDeployment( virtualFile );
    }

    protected String addDeployment(URL url) throws DeploymentException, URISyntaxException {
        VirtualFile file = VFS.getChild( url.toURI() );
        return addDeployment( file );
    }

    protected String addDeployment(VirtualFile file) throws DeploymentException {
        VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment( file );
        return addDeployment( deployment );
    }

    protected String addDeployment(Deployment deployment) throws DeploymentException {
        MainDeployer mainDeployer = getMainDeployer();
        log.info( "add deployment: " + deployment );
        mainDeployer.addDeployment( deployment );
        return deployment.getName();
    }

    protected String createDeployment(String name) throws IOException, DeploymentException {
        File file = File.createTempFile( name, ".tmp" );
        file.deleteOnExit();
        return addDeployment( file );
    }

    protected KernelController getKernelController() {
        return this.server.getKernel().getController();
    }

    protected MainDeployer getMainDeployer() {
        return (MainDeployer) getKernelController().getInstalledContext( MC_MAIN_DEPLOYER_NAME ).getTarget();
    }

    protected void processDeployments() throws DeploymentException {
        processDeployments( false );
    }

    protected void processDeployments(boolean checkComplete) throws DeploymentException {
        getMainDeployer().process();
        if (checkComplete) {
            getMainDeployer().checkComplete();
        }
    }

    protected ManagedDeployment getDeployment(String name) throws DeploymentException {
        return getMainDeployer().getManagedDeployment( name );
    }

    protected DeploymentUnit getDeploymentUnit(String name) {
        return ((MainDeployerImpl) getMainDeployer()).getDeploymentUnit( name );
    }

    protected BeanMetaData getBeanMetaData(DeploymentUnit unit, String name) {
        Set<? extends BeanMetaData> metaData = unit.getAllMetaData( BeanMetaData.class );

        for (BeanMetaData each : metaData) {
            if (each.getName().equals( name )) {
                return each;
            }
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    protected Object getBean(String name) {

        KernelRegistryEntry entry = getKernelController().getKernel().getRegistry().findEntry( name );
        if (entry == null) {
            return null;
        }

        return entry.getTarget();
    }

    protected void undeploy(String name) throws DeploymentException {
        getMainDeployer().undeploy( name );
        processDeployments( true );
    }

    protected JavaArchive createJar(String name) {
        return ShrinkWrap.create( JavaArchive.class, name );
    }

    protected File createJarFile(JavaArchive archive) throws IOException {
        return createJarFile( archive, ".jar" );
    }

    protected File createJarFile(JavaArchive archive, String suffix) throws IOException {
        File archiveFile = File.createTempFile( archive.getName(), suffix );
        archive.as( ZipExporter.class ).exportZip( archiveFile, true );
        archiveFile.deleteOnExit();
        return archiveFile;
    }
}
