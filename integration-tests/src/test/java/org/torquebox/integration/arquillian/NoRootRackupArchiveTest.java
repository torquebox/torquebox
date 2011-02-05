package org.torquebox.integration.arquillian;

import static org.junit.Assert.*;
import org.junit.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class NoRootRackupArchiveTest extends AbstractIntegrationTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "rack/1.1.0/norootrackup-archive-knob.yml" );
    }

    @Test
    public void testHappiness() {
        driver.get( "http://localhost:8080/norootrackuparchive" );
        WebElement body = driver.findElementByTagName( "body" );
        String text = body.getText().trim();

        String pwd = pwd().replaceAll( "\\\\", "/" );

        assertTrue( text.matches( "RACK_ROOT=" + toVfsPath( pwd ) + ".*/norootrackup.rack.*" ) );
    }

}
