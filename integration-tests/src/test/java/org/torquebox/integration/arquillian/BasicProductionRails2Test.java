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
public class BasicProductionRails2Test extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "rails/2.x/basic-production-rails.yml" );
	}

	@Test
	public void testHighLevel() {
        driver.get( "http://localhost:8080/basic-production-rails" );
        WebElement element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "basic-rails", element.getAttribute( "class" ) );
	}
	
	@Test
	public void testCaching() {
	    File cacheDir = new File( "apps/rails/2.x/basic/tmp/cache/views" );
	    
	    if ( cacheDir.exists() ) {
	        System.err.println( "deleting cache dir" );
	        if ( cacheDir.delete() ) {
	            System.err.println( "removed: " + cacheDir.getAbsolutePath() );
	        } else {
	            System.err.println( "error removing: " + cacheDir.getAbsolutePath() );
	        }
	    } else {
	        System.err.println( "no dir: " + cacheDir.getAbsolutePath() );
	    }
	    
        WebElement element = null;
        
        driver.get( "http://localhost:8080/basic-production-rails/cachey?value=taco" );
        element = driver.findElementById( "fragment" );
        assertNotNull( element );
        assertEquals( "one-taco-two", element.getText() );
        
        
        driver.get( "http://localhost:8080/basic-production-rails/cachey?value=gouda" );
        element = driver.findElementById( "fragment" );
        assertNotNull( element );
        assertEquals( "one-taco-two", element.getText() );
        
        File cacheFile = new File( cacheDir, "localhost.8080/cachey.cache" );
        
        assertTrue( cacheFile.exists() );
        
        cacheFile.delete();
        
        assertFalse( cacheFile.exists() );
        
        driver.get( "http://localhost:8080/basic-production-rails/cachey?value=jimi" );
        element = driver.findElementById( "fragment" );
        assertNotNull( element );
        assertEquals( "one-jimi-two", element.getText() );
        
        assertTrue( cacheFile.exists() );
	}
	

}
