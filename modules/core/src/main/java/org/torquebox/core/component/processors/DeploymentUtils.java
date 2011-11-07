package org.torquebox.core.component.processors;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;

/**
 * Utilities class for deployments.
 * @author mdobozy
 *
 */
public class DeploymentUtils {

    private static final AttachmentKey<Boolean> KEY = AttachmentKey.create( Boolean.class );

    public static void markUnitAsRootless(DeploymentUnit unit) {
        unit.putAttachment( KEY, true );
    }

    public static boolean isUnitRootless(DeploymentUnit unit) {
        Boolean value = unit.getAttachment( KEY );
        return value != null && value.booleanValue();
    }

    public static boolean isUnitRooted(DeploymentUnit unit) {
        return !isUnitRootless( unit );
    }

}
