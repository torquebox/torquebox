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

package org.torquebox.integration.arquillian.override;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

public abstract class AbstractOverrideTestCase extends AbstractIntegrationTestCase {

    protected String context;
    protected String app;
    protected String home;
    protected String env;

    @Test
    public void testStaticContent() {
        driver.get( "http://localhost:8080/" + context + "/index.html" );
        WebElement element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( app, element.getAttribute( "class" ) );
    }

    @Test
    @Ignore
    public void testRackRoot() {
        assertEquals( "vfs:" + System.getProperty( "basedir" ) + home, getEnvironmentVariable( "RACK_ROOT" ) );
    }

    @Test
    public void testRackEnv() {
        assertEquals( env, getEnvironmentVariable( "RACK_ENV" ) );
    }

    @Test
    public void testEnvironmentVariables() {
        assertEquals( app, getEnvironmentVariable( "APP" ) );
        assertEquals( app + " foo", getEnvironmentVariable( "foo" ) );
        assertEquals( app + " bar", getEnvironmentVariable( "bar" ) );
    }

    protected String getEnvironmentVariable(String name) {
        String url = "http://localhost:8080/" + context + "/" + name;
        driver.get( url );
        WebElement body = driver.findElementByTagName( "body" );
        return body.getText().trim();
    }
}
