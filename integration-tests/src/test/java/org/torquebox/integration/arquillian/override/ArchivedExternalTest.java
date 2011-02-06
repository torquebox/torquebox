package org.torquebox.integration.arquillian.override;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

@Run(RunModeType.AS_CLIENT)
public class ArchivedExternalTest extends ExplodedExternalTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "sinatra/1.0/archived-external-knob.yml" );
    }

    public ArchivedExternalTest() {
        home = "/apps/sinatra/1.0/override.rack";
    }

}
