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
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

public class ClassLoaderBootstrapper implements BeanMetaDataFactory {

    private VFSClassLoaderFactory factory;
    private Kernel kernel;

    public ClassLoaderBootstrapper() throws IOException {
    }

    public void create() throws Throwable {
        this.factory = new VFSClassLoaderFactory( "torquebox-bootstrap" );
        factory.setParentDomain( "DefaultDomain" );
        factory.setImportAll( true );
        factory.setExportAll( ExportAll.ALL );
        factory.setRoots( getRoots() );
    }

    @Override
    public List<BeanMetaData> getBeans() {
        List<BeanMetaData> beans = this.factory.getBeans();
        return beans;
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    public void start() throws Throwable {
        KernelController controller = this.kernel.getController();
        for (BeanMetaData each : getBeans()) {
            controller.install( each );
        }

    }

    public void stop() throws IOException {
    }

    protected List<String> getRoots() throws IOException, URISyntaxException {

        VirtualFile jrubyHome = getJRubyHome();

        List<String> paths = new ArrayList<String>();

        if ((jrubyHome != null) && (jrubyHome.exists())) {
            VirtualFile jrubyHomeLib = jrubyHome.getChild( "lib" );

            List<VirtualFile> children = jrubyHomeLib.getChildren();

            for (VirtualFile child : children) {
                if (child.getName().indexOf( "jboss" ) >= 0 || child.getName().indexOf( "log4j" ) >= 0) {
                    continue;
                }
                paths.add( child.toURI().toString() );
            }
        }

        String jbossHomeProp = System.getProperty( "jboss.home" );

        VirtualFile jbossHome = VFS.getRootVirtualFile().getChild( jbossHomeProp );

        VirtualFile tbLib = jbossHome.getChild( "common/torquebox" );

        for (VirtualFile child : tbLib.getChildren()) {
            paths.add( child.toURI().toString() );
        }

        return paths;
    }

    protected VirtualFile getJRubyHome() {
        String path = getJRubyHomePath();

        if (path == null) {
            return null;
        }

        return VFS.getChild( path );
    }

    protected String getJRubyHomePath() {
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

    protected String getJRubyHomeDefault() {
        String jbossHome = System.getProperty( "jboss.home" );

        if (jbossHome != null) {
            File candidatePath = new File( jbossHome, "../jruby" );
            if (candidatePath.exists() && candidatePath.isDirectory()) {
                return candidatePath.getAbsolutePath();
            }
        }

        return null;
    }

}
