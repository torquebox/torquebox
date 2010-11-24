package org.torquebox.integration.arquillian;

import static org.junit.Assert.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class SessionRails2Test extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "rails/2.3.8/basic-rails.yml" );
	}

	@Test
	public void testHighLevel() {
	    Options options = driver.manage();
        WebElement element = null;
        
        String cookieValue = null;
	    
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        assertEquals( 1, options.getCookies().size() );
        assertNotNull( options.getCookieNamed("JSESSIONID" ) );
        cookieValue = options.getCookieNamed("JSESSIONID" ).getValue();
        
        assertNotNull(cookieValue);
        
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );
        
        driver.get( "http://localhost:8080/basic-rails/sessioning/set_value" );
        assertEquals( 1, options.getCookies().size() );
        assertEquals( cookieValue, options.getCookieNamed( "JSESSIONID" ).getValue());
        
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );
        
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        assertEquals( 1, options.getCookies().size() );
        assertEquals( cookieValue, options.getCookieNamed( "JSESSIONID" ).getValue() );
        
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );
        
        driver.get( "http://localhost:8080/basic-rails/sessioning/clear_value" );
        assertEquals( 1, options.getCookies().size() );
        assertEquals( cookieValue, options.getCookieNamed( "JSESSIONID" ).getValue() );
        
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );
        
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        assertEquals( 1, options.getCookies().size() );
        assertEquals( cookieValue, options.getCookieNamed( "JSESSIONID" ).getValue() );
        
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );
	}

}
