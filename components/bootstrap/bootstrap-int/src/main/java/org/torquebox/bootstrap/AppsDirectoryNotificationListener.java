package org.torquebox.bootstrap;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.jboss.bootstrap.spi.server.ServerProvider;
import org.jboss.logging.Logger;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;

public class AppsDirectoryNotificationListener implements NotificationListener {

    public AppsDirectoryNotificationListener(ProfileService profileService, ProfileKey profileKey) {
        this.profileService = profileService;
        this.profileKey = profileKey;
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        if (notification.getType().equals( ServerProvider.START_NOTIFICATION_TYPE )) {
            try {
                activate();
            } catch (Exception e) {
                log.error( "Unable to activate apps/ deployment directory", e );
            }
        } else if (notification.getType().equals( ServerProvider.START_NOTIFICATION_TYPE )) {
            try {
                deactivate();
            } catch (Exception e) {
                log.error( "Unable to deactivate apps/ deployment directory", e );
            }
        }
    }

    protected void activate() throws Exception {
        log.info(  "Activating deployments dir"  );
        this.profileService.activateProfile( this.profileKey );
    }

    protected void deactivate() throws Exception {
        log.info(  "Deactivating deployments dir"  );
        this.profileService.deactivateProfile( this.profileKey );
    }

    private static final Logger log = Logger.getLogger( AppsDirectoryNotificationListener.class );

    private ProfileService profileService;
    private ProfileKey profileKey;

}
