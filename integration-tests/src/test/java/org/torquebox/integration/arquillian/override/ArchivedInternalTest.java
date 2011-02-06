package org.torquebox.integration.arquillian.override;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

@Run(RunModeType.AS_CLIENT)
public class ArchivedInternalTest extends ExplodedInternalTest {

    @Deployment
    public static JavaArchive createDeployment() throws Exception {
        return createDeployment( "sinatra/archived-internal-knob.yml" );
    }

    public ArchivedInternalTest() {
        home = "/apps/sinatra/override.knob";
    }
}
