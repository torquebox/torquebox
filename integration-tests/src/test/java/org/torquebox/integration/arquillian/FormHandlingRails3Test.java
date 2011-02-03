package org.torquebox.integration.arquillian;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class FormHandlingRails3Test extends AbstractIntegrationTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment("rails/3.0.0/basic-knob.yml");
    }

    @Test
    public void testFormOnFirstRender() {
        Options options = driver.manage();

        String cookieValue = null;

        driver.get("http://localhost:8080/basic-rails/form_handling");
        assertEquals(1, options.getCookies().size());
        assertNotNull(options.getCookieNamed("JSESSIONID"));
        cookieValue = options.getCookieNamed("JSESSIONID").getValue();
        assertNotNull(cookieValue);

        WebElement form = driver.findElementById("the-form");
        assertNotNull(form);

        WebElement authTokenInput = driver.findElementByXPath("//input[@name='authenticity_token']");
        assertNotNull(authTokenInput);

        String authToken = authTokenInput.getValue();
        assertNotNull(authToken);

        WebElement valueInput = driver.findElementById("the-value");
        assertNotNull(valueInput);
        assertEquals("", valueInput.getValue());

        valueInput.sendKeys("the value I submit");

        form.submit();

        valueInput = driver.findElementById("the-value");
        assertNotNull(valueInput);
        assertEquals("the value I submit is returned", valueInput.getValue());

        authTokenInput = driver.findElementByXPath("//input[@name='authenticity_token']");
        assertNotNull(authTokenInput);

        assertNotNull(authTokenInput.getValue());

        assertEquals(authToken, authTokenInput.getValue());
    }

    @Test
    public void testFlashUploadSessionUrlMatrix() {
        Options options = driver.manage();

        String cookieValue = null;

        driver.get("http://localhost:8080/basic-rails/form_handling/upload_file");
        assertEquals(1, options.getCookies().size());
        assertNotNull(options.getCookieNamed("JSESSIONID"));
        cookieValue = options.getCookieNamed("JSESSIONID").getValue();
        assertNotNull(cookieValue);
        
        WebElement form = driver.findElementById("the-upload-form");
        assertNotNull(form);
        
        WebElement authTokenInput = driver.findElementByXPath("//input[@name='authenticity_token']");
        assertNotNull(authTokenInput);
        
        String authToken = authTokenInput.getValue();
        assertNotNull(authToken);

        String action = form.getAttribute( "action" );
        
        assertTrue( action.contains( cookieValue ) );

        WebElement fileInput = driver.findElementById("the-file");
        
        URL fileUrl = getClass().getResource( "data.txt" );
        String filePath = fileUrl.getFile();
        
        fileInput.sendKeys( filePath );
        
        // CLEAR ALL COOKIES
        options.deleteAllCookies();
        
        form.submit();
        
        WebElement data = driver.findElementById("data");
        
        assertEquals( "Just some data. As returned.", data.getText().trim() );
        
        // No cookie should have been set, since session *was* passed.
        assertNull(options.getCookieNamed("JSESSIONID"));
    }

}
