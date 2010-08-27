package org.torquebox.integration;

import static org.junit.Assert.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class BasicSinatra_1_0_Test extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "sinatra/1.0/basic-sinatra-rack.yml" );
	}

	@Test
	public void testHighlevel() {
        driver.get( "http://localhost:8080/basic-sinatra" );
        WebElement element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "sinatra-basic", element.getAttribute( "class" ) );
	}
	
	@Test
	public void testRequestMapping() {
        driver.get( "http://localhost:8080/basic-sinatra/request-mapping" );
        WebElement element = driver.findElementById( "success" );
        assertNotNull( element );
        
        WebElement scheme = driver.findElementById( "scheme" );
        assertNotNull( element );
        assertEquals( "http", element.getText().trim() );
		
	}

}
