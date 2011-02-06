package org.torquebox.integration.arquillian.rack;

import static org.junit.Assert.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

@Run(RunModeType.AS_CLIENT)
public class BasicRack_1_1_0_Test extends AbstractIntegrationTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "rack/1.1.0/basic-knob.yml" );
    }

    @Test
    public void testHighlevel() {
        driver.get( "http://localhost:8080/basic-rack" );
        System.err.println( driver.getPageSource() );
        WebElement element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "basic-rack", element.getAttribute( "class" ) );
    }

}
