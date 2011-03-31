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
        System.err.println( " *************** START" );
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

            PropertyProfileSourceMetaData source = new PropertyProfileSourceMetaData( appsDir.getAbsolutePath() );
            metaData.setSource( source );
            log.info( "registering profile: " + metaData );
            this.key = this.profileService.registerProfile( metaData );
        } else {
            log.info( "Deployment directory does not exist: " + appsDir );
        }
    }

    public void start() throws Exception {
        log.info( "registering listener: " + this.key );
        this.listener = new AppsDirectoryNotificationListener( this.profileService, this.key );
        this.server.addNotificationListener( listener, null, null );
    }

    public void stop() throws Exception {
        log.info( "unregistering listener: " + this.key );
        this.server.removeNotificationListener( this.listener );
    }

    public void destroy() throws Exception {
        System.err.println( " *************** DESTROY" );
        this.profileService.unregisterProfile( this.key );
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
    private AbstractServer server;
    private ProfileKey key;
    private ProfileService profileService;

}
