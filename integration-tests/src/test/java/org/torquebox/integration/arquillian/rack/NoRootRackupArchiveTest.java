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
public class NoRootRackupArchiveTest extends AbstractIntegrationTestCase {

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
