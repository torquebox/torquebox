package org.torquebox.integration.arquillian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.junit.Test;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebElement;

public abstract class AbstractSessionRailsTest extends AbstractIntegrationTest {

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

	@Test
	public void testResetSession() {
        WebElement element = null;
	    
        // should have no value to begin with
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );
        
        // set a value in the session
        driver.get( "http://localhost:8080/basic-rails/sessioning/set_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );
        
        // get value from session
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );

        // logout to reset the session
        driver.get( "http://localhost:8080/basic-rails/sessioning/logout" );
        
        // should have no value after resetting session
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );
	}
	
	@Test
	public void testSessionViaMatrixUrl() {

        WebElement element = null;
	    Options options = driver.manage();
        
        // should have no value to begin with
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );
        
        // set a value in the session
        driver.get( "http://localhost:8080/basic-rails/sessioning/set_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );
        
        // get value from session
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );
        
        String cookieValue = options.getCookieNamed("JSESSIONID" ).getValue();
        
        assertNotNull(cookieValue);
        assertFalse( "".equals( cookieValue ) );
        
        options.deleteAllCookies();
        
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value;jsessionid=" + cookieValue );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );
        
        element = driver.findElementById( "session_id" );
        assertNotNull( element );
        assertEquals( cookieValue, element.getText().trim() );
        
        assertEquals( 0, options.getCookies().size() );
	}

}
