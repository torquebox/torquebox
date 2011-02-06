package org.torquebox.integration.arquillian.override;

import static org.junit.Assert.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

@Run(RunModeType.AS_CLIENT)
public class ExplodedExternalTest extends AbstractOverrideTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "sinatra/exploded-external-knob.yml" );
    }

    public ExplodedExternalTest() {
        context = "override-external";
        app = "external";
        home = "/apps/sinatra/override";
        env = "development";
    }

    public void testEnvironmentVariables() {
        assertEquals( app, getEnvironmentVariable( "APP" ) );
        assertEquals( "internal foo", getEnvironmentVariable( "foo" ) ); // not overridden
        assertEquals( "stink", getEnvironmentVariable( "foot" ) ); // extra
        assertEquals( "maid", getEnvironmentVariable( "bar" ) ); // overridden
    }

}
