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

package org.torquebox.integration.arquillian.rails3;

import static org.junit.Assert.*;

import java.io.File;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractOnTheFlyArchivingTestCase;

@Run(RunModeType.AS_CLIENT)
public class CachedJavascriptArchiveTest extends AbstractOnTheFlyArchivingTestCase {

    @Deployment
    public static JavaArchive createDeployment() throws Exception {
        File exploded = new File( System.getProperty( "user.dir" ) + "/apps/rails3/torque-174.knob" );

        return archive( exploded );
    }

    public void setUp() {
        super.setUp();
        driver.setJavascriptEnabled( true );
    }

    @Test
    public void testCachedJavascript() throws Exception {
        driver.get( "http://localhost:8080/torque-174/top" );
        WebElement element = driver.findElementById( "answer" );
        assertNotNull( element );
        assertEquals( "SUCCESS", element.getText().trim() );
    }
}
