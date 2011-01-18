package org.torquebox.integration.arquillian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.zip.ZipFile;

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
        //ZipFile app = new ZipFile( System.getProperty("user.dir") + "/apps/rails/3.0.0/torque-174.rails" );
        //return ShrinkWrap.create(ZipImporter.class, "torque-174.rails")
            //.importZip(app)
            //.as(JavaArchive.class);
        File exploded = new File( System.getProperty("user.dir") + "/apps/rails/3.0.0/torque-174.rails" );
        
	    return archive( exploded );
	}

    public void setUp() {
        super.setUp();
        driver.setJavascriptEnabled(true);
    }

    @Test
    public void testCachedJavascript() throws Exception {
        driver.get( "http://localhost:8080/torque-174/top" );
        WebElement element = driver.findElementById( "answer" );
        assertNotNull( element );
        assertEquals( "SUCCESS", element.getText().trim() );
    }
}
