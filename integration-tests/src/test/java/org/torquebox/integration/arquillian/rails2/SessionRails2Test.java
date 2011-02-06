package org.torquebox.integration.arquillian.rails2;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.torquebox.integration.arquillian.rails.CommonSessionRailsTest;

@Run(RunModeType.AS_CLIENT)
public class SessionRails2Test extends CommonSessionRailsTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "rails/2.x/basic-knob.yml" );
    }

}
