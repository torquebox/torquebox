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

import java.util.List;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

@Run(RunModeType.AS_CLIENT)
public class FrozenTest extends AbstractIntegrationTestCase {

    private static final String[] GEM_NAMES = { "railties", "activesupport", "actionpack", "activerecord", "actionmailer", "activeresource", };

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "rails2/frozen-knob.yml" );
    }

    @Test
    public void testHighLevel() {
        driver.get( "http://localhost:8080/frozen-rails" );
        //System.err.println("RESULT: ");
        //System.err.println(driver.getPageSource());
        WebElement element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "frozen-rails", element.getAttribute( "class" ) );

        List<WebElement> elements = driver.findElements( By.className( "load_path_element" ) );

        for (WebElement each : elements) {
            String pathElement = each.getText();

            // Ensure that the mentioned gems are loaded absolutely from our
            // frozen
            // vendored Rails, and not from system gems. Inspect the paths for
            // known elements that indicate frozenness.
            for (int i = 0; i < GEM_NAMES.length; ++i) {
                if (pathElement.contains( "/" + GEM_NAMES[i] + "/lib" )) {
                    String regex = "^.*frozen.*vendor/rails/" + GEM_NAMES[i] + "/lib.*$";
                    assert (pathElement.matches( regex ) );
                }
            }
        }
    }

}
