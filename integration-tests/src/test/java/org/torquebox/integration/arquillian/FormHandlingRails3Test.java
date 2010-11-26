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
public class FormHandlingRails3Test extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "rails/3.0.0/basic-rails.yml" );
	}

	@Test
	public void testHighLevel() {
	    Options options = driver.manage();
        
        String cookieValue = null;
	    
        driver.get( "http://localhost:8080/basic-rails/form_handling" );
        assertEquals( 1, options.getCookies().size() );
        assertNotNull( options.getCookieNamed("JSESSIONID" ) );
        cookieValue = options.getCookieNamed("JSESSIONID" ).getValue();
        assertNotNull(cookieValue);
        
        
        WebElement form = driver.findElementById( "the-form" );
        assertNotNull( form );
        
        WebElement authTokenInput = driver.findElementByXPath( "//input[@name='authenticity_token']");
        assertNotNull( authTokenInput );
        assertNotNull( authTokenInput.getValue() );
        
        WebElement valueInput = driver.findElementById( "the-value" );
        assertNotNull( valueInput );
        assertEquals( "", valueInput.getValue() );
        
        valueInput.sendKeys( "the value I submit" );
        
        form.submit();
        
        valueInput = driver.findElementById( "the-value" );
        assertNotNull( valueInput );
        assertEquals( "the value I submit", valueInput.getValue() );
	}

}
