package org.torquebox.integration.arquillian;

import static org.junit.Assert.*;
import org.junit.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class SinatraQueuesTest extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "sinatra/1.0/queues-rack.yml" );
	}

	@Test
	public void testTobyCrawley() throws Exception {
        driver.get( "http://localhost:8080/uppercaser/up/toby+crawley" );
        WebElement body = driver.findElementByTagName("body");
        assertEquals( "TOBY CRAWLEY", body.getText().trim());
	}

	@Test
	public void testEmployment() throws Exception {
        driver.get( "http://localhost:8080/uppercaser/job" );
        WebElement body = driver.findElementByTagName("body");
        assertEquals( "employment!", body.getText().trim());
	}

}
