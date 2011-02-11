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
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

@Run(RunModeType.AS_CLIENT)
public class BasicArchiveBackwardsCompatTest extends AbstractIntegrationTestCase {

    @Deployment
    public static JavaArchive createDeployment() throws Exception {
        JavaArchive archive = ShrinkWrap.create( JavaArchive.class, "archive.rails" );

        File railsApp = new File( System.getProperty( "user.dir" ) + "/apps/rails2/basic" );

        importDirectory( archive, railsApp );

        StringBuilder torqueboxYamlText = new StringBuilder();
        torqueboxYamlText.append( "application:\n" );
        torqueboxYamlText.append( "  RAILS_ENV: development\n" );
        torqueboxYamlText.append( "web:\n" );
        torqueboxYamlText.append( "  context: /archive-rails\n" );

        Asset torqueboxYaml = new StringAsset( torqueboxYamlText.toString() );
        archive.addResource( torqueboxYaml, "config/torquebox.yml" );

        return archive;
    }

    @Test
    public void testHighLevel() {
        driver.get( "http://localhost:8080/archive-rails" );
        WebElement element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "basic-rails", element.getAttribute( "class" ) );
    }

    static void importDirectory(JavaArchive archive, File directory) {
        importDirectory( archive, directory, "" );
    }

    static void importDirectory(JavaArchive archive, File directory, String path) {
        if (!path.equals( "" )) {
            archive.addDirectory( path );
        }

        for (File child : directory.listFiles()) {
            String childPath = null;
            if (path.equals( "" )) {
                childPath = child.getName();
            } else {
                childPath = path + "/" + child.getName();
            }

            if (child.isDirectory()) {
                importDirectory( archive, child, childPath );
            } else {
                archive.addResource( child, childPath );
            }
        }
    }

}
