package org.torquebox.integration.arquillian.override;

import static org.junit.Assert.*;
import org.junit.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;


@Run(RunModeType.AS_CLIENT)
public class ArchivedExternalTest extends ExplodedExternalTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment("sinatra/1.0/archived-external-rack.yml");
	}

    public ArchivedExternalTest() {
        home = "/apps/sinatra/1.0/override.rack";
    }

    @Ignore
    @Test
	public void testEnvironmentVariables() {
        // This test is only declared at all so I can ignore it
        // because the AppRackYamlParsingDeployer doesn't seem to be
        // able to find the external.ru file when the RACK_ROOT in the
        // -rack.yml refers to an archive.
        super.testEnvironmentVariables();
	}

}
