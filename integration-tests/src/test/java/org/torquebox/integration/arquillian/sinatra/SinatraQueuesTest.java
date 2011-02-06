package org.torquebox.integration.arquillian.sinatra;

import static org.junit.Assert.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

@Run(RunModeType.AS_CLIENT)
public class SinatraQueuesTest extends AbstractIntegrationTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "sinatra/1.0/queues-knob.yml" );
    }

    @Test
    public void testTobyCrawley() throws Exception {
        driver.get( "http://localhost:8080/uppercaser/up/toby+crawley" );
        WebElement body = driver.findElementByTagName( "body" );
        assertEquals( "TOBY CRAWLEY", body.getText().trim() );
    }

    @Test
    public void testEmployment() throws Exception {
        driver.get( "http://localhost:8080/uppercaser/job" );
        WebElement body = driver.findElementByTagName( "body" );
        assertEquals( "employment!", body.getText().trim() );
    }

}
