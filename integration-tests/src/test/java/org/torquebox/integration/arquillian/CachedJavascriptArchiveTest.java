package org.torquebox.integration.arquillian;

import static org.junit.Assert.*;

import java.io.File;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class CachedJavascriptArchiveTest extends AbstractOnTheFlyArchivingTest {

	@Deployment
	public static JavaArchive createDeployment() throws Exception {
        File exploded = new File( System.getProperty("user.dir") + "/apps/rails/3.0.0/torque-174.knob" );
        
	    return archive( exploded );
	}

    public void setUp() {
        super.setUp();
        driver.setJavascriptEnabled(true);
    }

    @Test
    public void testCachedJavascript() throws Exception {
        driver.get( "http://localhost:8080/torque-174/top" );
        System.err.println( "RESULT: " + driver.getPageSource() );
        WebElement element = driver.findElementById( "answer" );
        assertNotNull( element );
        assertEquals( "SUCCESS", element.getText().trim() );
    }
}
