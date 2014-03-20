/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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
        assertEquals( 600, rackMetaData.getSessionTimeout() );
    }

    @Test
    public void testNoUnitsSessionTimeout() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "timeout-nounits-web.yml" );

        RackMetaData rackMetaData = unit.getAttachment( RackMetaData.ATTACHMENT_KEY );

        assertNotNull( rackMetaData );

        assertEquals( "/tengwar", rackMetaData.getContextPath() );
        assertEquals( 1, rackMetaData.getHosts().size() );
        assertEquals( "mordor.com", rackMetaData.getHosts().get( 0 ) );
        assertNull( rackMetaData.getStaticPathPrefix() );
        assertEquals( 600, rackMetaData.getSessionTimeout() );
    }
    
    @Test
    public void testOldStyleDashedSessionTimeout() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "timeout-dashed-web.yml" );

        RackMetaData rackMetaData = unit.getAttachment( RackMetaData.ATTACHMENT_KEY );

        assertNotNull( rackMetaData );

        assertEquals( 600, rackMetaData.getSessionTimeout() );
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
    public void testValidContextPathWithSlashes() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "context-path-slashes.yml" );
        RackMetaData rackMetaData = unit.getAttachment( RackMetaData.ATTACHMENT_KEY );
        assertNotNull( rackMetaData );
        assertEquals( "/tacos/and/bacon", rackMetaData.getContextPath() );
    }

}
