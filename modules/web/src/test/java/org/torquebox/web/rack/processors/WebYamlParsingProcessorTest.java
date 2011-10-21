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

package org.torquebox.web.rack.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.torquebox.core.app.processors.AppKnobYamlParsingProcessor;
import org.torquebox.core.processors.TorqueBoxYamlParsingProcessor;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.web.rack.RackMetaData;

public class WebYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUp() {
        clearDeployers();
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new WebYamlParsingProcessor() );
    }

    @Test()
    public void testEmptyWebYml() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "empty.yml" );

        assertNull( unit.getAttachment( RackMetaData.ATTACHMENT_KEY ) );
    }

    @Test
    public void testValidWebYml() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "valid-web.yml" );

        RackMetaData rackMetaData = unit.getAttachment( RackMetaData.ATTACHMENT_KEY );

        assertNotNull( rackMetaData );

        assertEquals( "/tacos", rackMetaData.getContextPath() );
        assertEquals( 1, rackMetaData.getHosts().size() );
        assertEquals( "foobar.com", rackMetaData.getHosts().get( 0 ) );
        assertNull( rackMetaData.getStaticPathPrefix() );
    }

    @Test
    public void testValidWebYmlCustomStaticPathPrefix() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "static-path-web.yml" );

        RackMetaData rackMetaData = unit.getAttachment( RackMetaData.ATTACHMENT_KEY );

        assertNotNull( rackMetaData );

        assertEquals( "/tacos", rackMetaData.getContextPath() );
        assertEquals( 1, rackMetaData.getHosts().size() );
        assertEquals( "foobar.com", rackMetaData.getHosts().get( 0 ) );
        assertEquals( "/elsewhere", rackMetaData.getStaticPathPrefix() );
    }

    @Test
    public void testRootless() throws Exception {
        try {
            clearDeployers();
            appendDeployer( new AppKnobYamlParsingProcessor() );
            appendDeployer( new WebYamlParsingProcessor() );
            deployResourceAs( "rootless-web-knob.yml", "rootless-web-knob.yml" );
            fail( "Rootless web knob deployment should have failed." );
        } catch (DeploymentUnitProcessingException e) {
            assertEquals( "Error processing deployment rootless-web-knob.yml: The section web " +
                    "requires an app root to be specified, but none has been provided.",
                    e.getMessage() );
        }
    }

}
