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

package org.torquebox.core.injection.processors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.torquebox.core.injection.InjectionMetaData;
import org.torquebox.core.processors.TorqueBoxYamlParsingProcessor;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class InjectionYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new InjectionYamlParsingProcessor() );
    }

    @Test
    public void testEmptyInjectionYml() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "empty.yml" );
        assertFalse( unit.hasAttachment( InjectionMetaData.ATTACHMENT_KEY ) );

    }

    @Test(expected = DeploymentUnitProcessingException.class)
    public void testJunkPoolingYml() throws Exception {
        deployResourceAsTorqueboxYml( "junk-injection.yml" );
    }

    @Test
    public void testTrueInjectionYml() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "true-injection.yml" );
        InjectionMetaData injectionMetaData = unit.getAttachment( InjectionMetaData.ATTACHMENT_KEY );
        assertNotNull( injectionMetaData );
        assertTrue( injectionMetaData.isEnabled() );
    }

    @Test
    public void testFalseInjectionYml() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "false-injection.yml" );
        InjectionMetaData injectionMetaData = unit.getAttachment( InjectionMetaData.ATTACHMENT_KEY );
        assertNotNull( injectionMetaData );
        assertFalse( injectionMetaData.isEnabled() );
    }

    @Test
    public void testArrayPathsInjectionYml() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "array-paths-injection.yml" );
        InjectionMetaData injectionMetaData = unit.getAttachment( InjectionMetaData.ATTACHMENT_KEY );
        assertNotNull( injectionMetaData );
        assertEquals( injectionMetaData.getPaths().size(), 2 );

        String[] paths = {"foo", "bar"};

        assertArrayEquals( injectionMetaData.getPaths().toArray(), paths );
    }

    @Test
    public void testSimplePathInjectionYml() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "path-injection.yml" );
        InjectionMetaData injectionMetaData = unit.getAttachment( InjectionMetaData.ATTACHMENT_KEY );
        assertNotNull( injectionMetaData );
        assertEquals( injectionMetaData.getPaths().size(), 1 );

        String[] paths = {"foo"};

        assertArrayEquals( injectionMetaData.getPaths().toArray(), paths );
    }
}
