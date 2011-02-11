/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.integration.arquillian.rack;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

@Run(RunModeType.AS_CLIENT)
public class ReloaderRackTest extends AbstractIntegrationTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "rack/reloader-knob.yml" );
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

        Thread.sleep( 3000 );

        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "1", element.getText() );

        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "1", element.getText() );

        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "1", element.getText() );

        Thread.sleep( 3000 );

        driver.get( "http://localhost:8080/reloader-rack" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "4", element.getText() );
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
