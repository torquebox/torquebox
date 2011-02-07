package org.torquebox.integration.arquillian.rails2;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.torquebox.integration.arquillian.rails.CommonSessionHandlingTestCase;

@Run(RunModeType.AS_CLIENT)
public class SessionHandlingTest extends CommonSessionHandlingTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "rails2/basic-knob.yml" );
    }

}
