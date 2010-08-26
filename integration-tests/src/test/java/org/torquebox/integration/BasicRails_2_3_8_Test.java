package org.torquebox.integration;

import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class BasicRails_2_3_8_Test extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "rails/2.3.8/basic-rails.yml" );
	}

	@Test
	public void testSomething() {
        driver.get( "http://localhost:8080/testapp" );
        WebElement element = driver.findElementById( "success" );
        assertNotNull( element );
	}

}
