package org.torquebox.integration.arquillian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class FrozenRails_2_3_8_Test extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "rails/2.3.8/frozen-rails.yml" );
	}

	@Test
	@Ignore
	public void testHighLevel() {
        driver.get( "http://localhost:8080/frozen-rails" );
        System.err.println( "RESULT: " );
        System.err.println( driver.getPageSource() );
        WebElement element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "basic-rails", element.getAttribute( "class" ) );
	}

}
