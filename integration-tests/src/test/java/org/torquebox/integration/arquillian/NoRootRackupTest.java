package org.torquebox.integration.arquillian;

import static org.junit.Assert.*;
import org.junit.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class NoRootRackupTest extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "rack/1.1.0/norootrackup-rack.yml" );
	}

	@Test
	public void testHappiness() {
        driver.get( "http://localhost:8080/norootrackup" );
        WebElement body = driver.findElementByTagName("body");
        
        String expectedRoot = System.getProperty("basedir");
        
        System.err.println( "BOB: expectedRoot.1=" + expectedRoot );
        expectedRoot = expectedRoot.replaceAll( "\\\\", "/" );
        System.err.println( "BOB: expectedRoot.2=" + expectedRoot );
        expectedRoot = toVfsPath( expectedRoot + "/apps/rack/1.1.0/norootrackup" );
        System.err.println( "BOB: expectedRoot.3=" + expectedRoot );
        
        assertEquals("RACK_ROOT=" + expectedRoot, body.getText().trim());
	}

}
