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
public class NoRootRackupTest extends AbstractIntegrationTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "rack/norootrackup-knob.yml" );
    }

    @Test
    public void testHappiness() {
        driver.get( "http://localhost:8080/norootrackup" );
        WebElement body = driver.findElementByTagName( "body" );
        String expectedRoot = System.getProperty( "basedir" );
        expectedRoot = expectedRoot.replaceAll( "\\\\", "/" );
        expectedRoot = toVfsPath( expectedRoot + "/apps/rack/norootrackup" );

        assertEquals( "RACK_ROOT=" + expectedRoot, body.getText().trim() );
    }

}
