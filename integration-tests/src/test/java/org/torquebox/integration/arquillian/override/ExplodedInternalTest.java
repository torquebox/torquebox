package org.torquebox.integration.arquillian.override;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

@Run(RunModeType.AS_CLIENT)
public class ExplodedInternalTest extends AbstractOverrideTestCase {

    @Deployment
    public static JavaArchive createDeployment() throws Exception {
        return createDeployment( "sinatra/exploded-internal-knob.yml" );
    }

    public ExplodedInternalTest() {
        context = "override-internal";
        app = "internal";
        home = "/apps/sinatra/override";
        env = "production";
    }

}
