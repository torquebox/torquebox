package org.torquebox.integration.arquillian.rails2;

import static org.junit.Assert.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

@Run(RunModeType.AS_CLIENT)
public class BasicRails2Test extends AbstractIntegrationTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "rails2/basic-knob.yml" );
    }

    @Test
    public void testHighLevel() {
        driver.get( "http://localhost:8080/basic-rails" );
        WebElement element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "basic-rails", element.getAttribute( "class" ) );
    }

    @Test
    public void testSendData() {
        driver.get( "http://localhost:8080/basic-rails/senddata" );
        String content = driver.getPageSource();
        assertNotNull( content );
        assertEquals( "this is the content", content );

    }

    @Test
    public void testSendFile() {
        driver.get( "http://localhost:8080/basic-rails/sendfile" );
        String content = driver.getPageSource();
        assertNotNull( content );
        assertEquals( "this is the contents of the file", content.trim() );

    }

}
