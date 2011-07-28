package org.torquebox.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.logging.Logger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.SuffixMatchFilter;

public class Bootstrap implements BeanMetaDataFactory {
    
    private static Logger log = Logger.getLogger( Bootstrap.class );

    private KernelController controller;
    private List<Object> contextNames = new ArrayList<Object>();
    private List<BeanMetaData> beans;
    private VirtualFile jrubyHome;
    public static final VirtualFileFilter JAR_FILTER = new SuffixMatchFilter( ".jar", VisitorAttributes.DEFAULT );
    
    public Bootstrap() {
    }

    public void create() throws Throwable {
        log.info( "Bootstrapping TorqueBox AS" );
        determineJRubyHome();
        createClassLoaderBeans();
    }

    protected void createClassLoaderBeans() throws IOException, URISyntaxException {
        VFSClassLoaderFactory factory = new VFSClassLoaderFactory( "torquebox-bootstrap" );
        factory.setParentDomain( "DefaultDomain" );
        factory.setImportAll( true );
        factory.setExportAll( ExportAll.ALL );
        log.debug( "Roots: " + getRoots() );
        factory.setRoots( getRoots() );
        this.beans = factory.getBeans();
    }

    protected void determineJRubyHome() throws DeploymentException, IOException {
        this.jrubyHome = getJRubyHomeVirtualFile();

        if (!this.jrubyHome.exists()) {
            throw new DeploymentException( "JRUBY_HOME does not exist: " + this.jrubyHome );
        }
        
        System.setProperty( "jruby.home", this.jrubyHome.getPathName() );
        
        log.info( "Using JRuby: " + this.jrubyHome );
    }

    @Override
    public List<BeanMetaData> getBeans() {
        return beans;
    }

    public void setKernelController(KernelController controller) {
        this.controller = controller;
    }

    public void start() throws Throwable {
        for (BeanMetaData each : getBeans()) {
            this.contextNames.add( this.controller.install( each ).getName() );
        }
    }

    public void stop() throws IOException {
        for (Object name : this.contextNames) {
            this.controller.uninstall( name );
        }
    }

    protected List<String> getRoots() throws IOException, URISyntaxException {
        List<String> paths = new ArrayList<String>();

        paths.addAll( getJRubyRoots() );
        paths.addAll( getTorqueBoxRoots() );
        
        return paths;
    }

    protected List<String> getJRubyRoots() throws IOException, URISyntaxException {
        return getJarFiles( getJRubyHome().getChild( "lib" ) );
    }
    
    protected List<String> getTorqueBoxRoots() throws IOException, URISyntaxException { 
        return getJarFiles( getJBossHome().getChild( "common" ).getChild( "torquebox" ) );
    }
    
    protected List<String> getJarFiles(VirtualFile dir) throws IOException, URISyntaxException {
        List<String> jars = new ArrayList<String>();
        
        for(VirtualFile each : dir.getChildren( JAR_FILTER )) {
            jars.add(  each.toURI().toString() );
        }
        
        return jars;
    }
    
    protected VirtualFile getJBossHome() {
        return VFS.getChild( System.getProperty( "jboss.home"  )  );
    }

    protected VirtualFile getJRubyHome() {
        return this.jrubyHome;
    }
    
    protected VirtualFile getJRubyHomeVirtualFile() throws IOException {
        String path = getJRubyHomePath();
        if ( path == null ) {
            return null;
        }
        return VFS.getChild( path );
    }

    protected String getJRubyHomePath() throws IOException {
        String jrubyHome = getJRubyHomeSysProp();

        if (jrubyHome != null) {
            return jrubyHome;
        }

        jrubyHome = getJRubyEnvVar();

        if (jrubyHome != null) {
            return jrubyHome;
        }

        return getJRubyHomeDefault();
    }

    protected String getJRubyHomeSysProp() {
        return System.getProperty( "jruby.home" );
    }

    protected String getJRubyEnvVar() {
        if (!"true".equals( System.getProperty( "jruby_home.env.ignore" ) )) {
            return System.getenv( "JRUBY_HOME" );
        }

        return null;
    }

    protected String getJRubyHomeDefault() throws IOException {
        String jbossHome = System.getProperty( "jboss.home" );
        
        if (jbossHome != null) {
            File candidatePath = new File( new File( jbossHome, ".." ), "jruby" );
            if (candidatePath.exists() && candidatePath.isDirectory()) {
                return candidatePath.getCanonicalPath();
            }
        }

        return null;
    }

}
