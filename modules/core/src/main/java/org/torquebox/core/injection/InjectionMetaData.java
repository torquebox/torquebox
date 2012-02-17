package org.torquebox.core.injection;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;

public class InjectionMetaData {

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static final AttachmentKey<InjectionMetaData> ATTACHMENT_KEY = AttachmentKey.create( InjectionMetaData.class );    
    
    public static boolean injectionIsEnabled(DeploymentUnit unit) {
        InjectionMetaData injectionMetaData = unit.getAttachment( ATTACHMENT_KEY );
        return (injectionMetaData == null || injectionMetaData.isEnabled());
    }
    
}
