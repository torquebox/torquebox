package org.torquebox.integration.arquillian;

import static org.junit.Assert.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class ReloaderRackTest extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "rack/1.1.0/reloader-rack.yml" );
	}

	@Test
	public void testReloading() throws InterruptedException {
        WebElement element = null;
        
        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "INITIAL", element.getText() );
        
        Thread.sleep( 3000 );
        
        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "0", element.getText() );
        
        Thread.sleep( 3000 );
        
        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "1", element.getText() );
        
        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "1", element.getText() );
        
        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "1", element.getText() );
        
        Thread.sleep( 3000 );
        
        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "4", element.getText() );
	}
	
}
