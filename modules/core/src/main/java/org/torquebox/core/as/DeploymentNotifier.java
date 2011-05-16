package org.torquebox.core.as;

import java.util.Date;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class DeploymentNotifier implements Service<Void> {
    
    public static final AttachmentKey<AttachmentList<ServiceName>> SERVICES_ATTACHMENT_KEY = AttachmentKey.createList( ServiceName.class );
    public static final AttachmentKey<Long> DEPLOYMENT_TIME_ATTACHMENT_KEY = AttachmentKey.create( Long.class );
    
    public DeploymentNotifier(DeploymentUnit unit) {
        this.unit = unit;
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    @Override
    public void start(StartContext context) throws StartException {
        long startTime = unit.getAttachment( DeploymentNotifier.DEPLOYMENT_TIME_ATTACHMENT_KEY );
        long elapsed = System.currentTimeMillis() - startTime;
        log.info( "Completely deployed: " + unit.getName() + " in " + elapsed + "ms" );
    }

    @Override
    public void stop(StopContext context) {
        
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.core.as" );
    
    private DeploymentUnit unit;


}
