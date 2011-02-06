package org.torquebox.integration.arquillian.rack;

import static org.junit.Assert.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.util.Base64;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

@Run(RunModeType.AS_CLIENT)
public class BasicAuthRack_1_1_0_Test extends AbstractIntegrationTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "rack/1.1.0/basic-auth-knob.yml" );
    }

    @Test
    public void testBasicAuth() {

        String creds = "bmcwhirt@redhat.com:swordfish";
        String encodedCreds = Base64.encodeBytes( creds.getBytes() );

        driver.setCredentials( "bmcwhirt@redhat.com", "swordfish" );

        driver.get( "http://localhost:8080/basic-auth" );

        WebElement element = driver.findElementById( "auth_header" );
        assertNotNull( element );
        assertEquals( "Basic " + encodedCreds, element.getText().trim() );
    }

}
