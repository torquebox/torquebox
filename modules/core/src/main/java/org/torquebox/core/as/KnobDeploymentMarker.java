package org.torquebox.core.as;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;

public class KnobDeploymentMarker {
    
    private static final AttachmentKey<Boolean> MARKER = AttachmentKey.create(Boolean.class);
    
    public static boolean isMarked(DeploymentUnit unit) {
        return unit.hasAttachment( MARKER );
    }
    
    public static void applyMark(DeploymentUnit unit) {
        unit.putAttachment( MARKER, Boolean.TRUE );
    }


}
