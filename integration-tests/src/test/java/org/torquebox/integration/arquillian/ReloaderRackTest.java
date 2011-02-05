package org.torquebox.integration.arquillian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.AssertTrue;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class ReloaderRackTest extends AbstractIntegrationTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "rack/1.1.0/reloader-knob.yml" );
    }

    @Test
    public void testReloading() throws InterruptedException {
        if (isWindows()) {
            testReloadingOnWindows();
        } else {
            testReloadingOnUnix();
        }
    }

    // DO NOT MARK AS @Test
    public void testReloadingOnUnix() throws InterruptedException {
        WebElement element = null;

        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "INITIAL", element.getText() );

        Thread.sleep( 3000 );

        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "0", element.getText() );
        System.err.println( "==>" + element.getText() );

        Thread.sleep( 3000 );

        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "1", element.getText() );
        System.err.println( "==>" + element.getText() );

        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "1", element.getText() );
        System.err.println( "==>" + element.getText() );

        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "1", element.getText() );
        System.err.println( "==>" + element.getText() );

        Thread.sleep( 3000 );

        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "4", element.getText() );
        System.err.println( "==>" + element.getText() );
    }

    // DO NOT MARK AS @Test
    public void testReloadingOnWindows() throws InterruptedException {
        WebElement element = null;

        Set<String> seenValues = new HashSet<String>();

        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "INITIAL", element.getText() );

        seenValues.add( element.getText() );

        for (int i = 0; i < 10; ++i) {
            Thread.sleep( 3000 );
            driver.get( "http://localhost:8080/reloader-rack" );
            element = driver.findElementById( "success" );
            assertNotNull( element );
            seenValues.add( element.getText() );
        }

        assertTrue( seenValues.size() >= 3 );
    }

}
