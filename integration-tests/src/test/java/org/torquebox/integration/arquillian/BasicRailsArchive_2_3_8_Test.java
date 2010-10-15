package org.torquebox.integration.arquillian;

import static org.junit.Assert.*;
import java.util.zip.ZipFile;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.junit.Test;
import org.openqa.selenium.WebElement;


@Run(RunModeType.AS_CLIENT)
public class BasicRailsArchive_2_3_8_Test extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() throws Exception {
        ZipFile app = new ZipFile( System.getProperty("user.dir") + "/apps/rails/2.3.8/basic.rails" );
        return ShrinkWrap.create(ZipImporter.class, "archive.rails")
            .importZip(app)
            .as(JavaArchive.class);
	}

	@Test
	public void testHighLevel() {
        driver.get( "http://localhost:8080/archive-rails" );
        WebElement element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "basic-rails", element.getAttribute( "class" ) );
	}

}
