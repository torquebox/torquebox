package org.torquebox.integration.arquillian;

import static org.junit.Assert.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class NoRootRackupTest extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "rack/1.1.0/norootrackup-rack.yml" );
	}

	@Test
	public void testHappiness() {
        driver.get( "http://localhost:8080/norootrack" );
        WebElement body = driver.findElementByTagName("body");
        assertTrue( body.getText().trim().startsWith("RACK_ROOT") );
	}

}
