package org.torquebox.integration.arquillian;

import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.WebElement;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import java.util.zip.ZipFile;

@Run(RunModeType.AS_CLIENT)
public class CachedJavascriptArchiveTest extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() throws Exception {
        ZipFile app = new ZipFile( System.getProperty("user.dir") + "/apps/rails/3.0.0/torque-174.rails" );
        return ShrinkWrap.create(ZipImporter.class, "torque-174.rails")
            .importZip(app)
            .as(JavaArchive.class);
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
