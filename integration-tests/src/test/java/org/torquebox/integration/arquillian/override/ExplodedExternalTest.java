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

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

@Run(RunModeType.AS_CLIENT)
public class ExplodedExternalTest extends AbstractOverrideTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "sinatra/exploded-external-knob.yml" );
    }

    public ExplodedExternalTest() {
        context = "override-external";
        app = "external";
        home = "/apps/sinatra/override";
        env = "development";
    }

    public void testEnvironmentVariables() {
        assertEquals( app, getEnvironmentVariable( "APP" ) );
        assertEquals( "internal foo", getEnvironmentVariable( "foo" ) ); // not overridden
        assertEquals( "stink", getEnvironmentVariable( "foot" ) ); // extra
        assertEquals( "maid", getEnvironmentVariable( "bar" ) ); // overridden
    }

}
