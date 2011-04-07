package org.torquebox.bootstrap;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.management.NotificationListener;

import org.jboss.bootstrap.impl.base.server.AbstractServer;
import org.jboss.logging.Logger;
import org.jboss.profileservice.profile.metadata.plugin.HotDeploymentProfileMetaData;
import org.jboss.profileservice.profile.metadata.plugin.PropertyProfileSourceMetaData;
import org.jboss.profileservice.profile.metadata.plugin.ScanPeriod;
import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

public class AppsDirectoryBootstrapper {

    public AppsDirectoryBootstrapper() {

    }

    public void setServer(AbstractServer server) {
        this.server = server;
    }

    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
    }

    public void create() throws Exception {
        File appsDir = getAppsDir();

        if (appsDir.exists()) {
            log.info( "Adding deployment directory: " + appsDir );
            HotDeploymentProfileMetaData metaData = new HotDeploymentProfileMetaData();
            metaData.setName( "torquebox.apps.dir" );

            ScanPeriod scanPeriod = new ScanPeriod();
            scanPeriod.setStartAutomatically( true );
            scanPeriod.setScanPeriod( 5 );
            scanPeriod.setTimeUnit( TimeUnit.SECONDS );
            metaData.setScanPeriod( scanPeriod );

            PropertyProfileSourceMetaData source = new PropertyProfileSourceMetaData( getSanitizedPath( appsDir.getPath() ) );
            metaData.setSource( source );
            this.key = this.profileService.registerProfile( metaData );
        } else {
            this.key = null;
            log.info( "Deployment directory does not exist: " + appsDir );
        }
    }

    protected String getSanitizedPath(String path) {
        String sanitizedPath = null;

        if (path.indexOf( "\\\\" ) >= 0) {
            sanitizedPath = path.replaceAll( "\\\\\\\\", "/" );
            sanitizedPath = sanitizedPath.replaceAll( "\\\\", "" );
        } else {
            sanitizedPath = path.replaceAll( "\\\\", "/" );
        }
        if (!sanitizedPath.startsWith( "/" )) {
            sanitizedPath = "/" + sanitizedPath;
        }
        
        return sanitizedPath;
    }

    public void start() throws Exception {
        if (this.key != null) {
            this.listener = new AppsDirectoryNotificationListener( this.profileService, this.key );
            this.server.addNotificationListener( listener, null, null );
        }
    }

    public void stop() throws Exception {
        if (this.listener != null) {
            this.server.removeNotificationListener( this.listener );
        }
    }

    public void destroy() throws Exception {
        if (this.key != null) {
            this.profileService.unregisterProfile( this.key );
        }
        this.key = null;
    }

    protected File getAppsDir() {
        String torqueboxHome = System.getProperty( "torquebox.home" );
        if (torqueboxHome == null || torqueboxHome.equals( "" )) {
            torqueboxHome = System.getenv( "TORQUEBOX_HOME" );
        }

        if (torqueboxHome == null || torqueboxHome.equals( "" )) {
            torqueboxHome = new File( System.getProperty( "jboss.home" ) ).getParent();
        }

        File appDir = new File( torqueboxHome, "apps" );
        return appDir;
    }

    private static final Logger log = Logger.getLogger( AppsDirectoryBootstrapper.class );

    private AppsDirectoryNotificationListener listener;

    @SuppressWarnings("rawtypes")
    private AbstractServer server;
    private ProfileKey key;
    private ProfileService profileService;

}
