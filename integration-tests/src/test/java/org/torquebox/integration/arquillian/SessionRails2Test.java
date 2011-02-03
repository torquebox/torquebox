package org.torquebox.integration.arquillian;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

@Run(RunModeType.AS_CLIENT)
public class SessionRails2Test extends CommonSessionRailsTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment("rails/2.x/basic-knob.yml");
    }

}
