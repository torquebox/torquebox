package org.torquebox.integration;

import java.net.URL;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

@RunWith(Arquillian.class)
public abstract class AbstractIntegrationTest {

    protected HtmlUnitDriver driver;
    
	@Before
	public void setUp() {
        this.driver = new HtmlUnitDriver();
	}

	@After
	public void tearDown() {
		this.driver = null;
	}
	
	public HtmlUnitDriver getDriver() {
		return this.driver;
	}
	
	public static JavaArchive createDeployment(String name) {
		int lastSlashLoc = name.lastIndexOf( '/' );
		String tail = name.substring( lastSlashLoc + 1 );
		int lastDot = tail.lastIndexOf( '.' );
		String base = tail.substring(0, lastDot );
		
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class, base + ".jar" );
		ClassLoader classLoader = AbstractIntegrationTest.class.getClassLoader();
		URL deploymentDescriptorUrl = classLoader.getResource( name );
		archive.addResource( deploymentDescriptorUrl, "/META-INF/" + tail );
		return archive;
	}

}
