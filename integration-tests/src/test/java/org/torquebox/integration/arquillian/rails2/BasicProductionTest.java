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

package org.torquebox.integration.arquillian.rails2;

import static org.junit.Assert.*;

import java.io.File;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

@Run(RunModeType.AS_CLIENT)
public class BasicProductionTest extends AbstractIntegrationTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "rails2/basic-production-knob.yml" );
    }

    @Test
    @Ignore
    public void testHighLevel() {
        driver.get( "http://localhost:8080/basic-production-rails" );
        WebElement element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "basic-rails", element.getAttribute( "class" ) );
    }

    @Test
    public void testCaching() throws Exception {
        File cacheDir = new File( "apps/rails2/basic/tmp/cache/views" );

        rm( cacheDir );

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
