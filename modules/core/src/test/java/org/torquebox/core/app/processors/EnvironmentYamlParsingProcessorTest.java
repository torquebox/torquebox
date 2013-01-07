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

package org.torquebox.core.app.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.projectodd.polyglot.core.util.DeprecationLogger;
import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.processors.TorqueBoxYamlParsingProcessor;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class EnvironmentYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUp() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new EnvironmentYamlParsingProcessor() );
        appendDeployer( new RubyApplicationDefaultsProcessor() );
    }

    @Test
    public void testBooleanEnvironmentYml() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "environment.yml" );

        RubyAppMetaData appMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
        assertNotNull( appMetaData );

        Map<String, String> environmentVariables = appMetaData.getEnvironmentVariables();
        assertNotNull( environmentVariables );

        String booleanVariable = environmentVariables.get( "A_BOOLEAN_VALUE" );
        assertEquals( "true", booleanVariable );
    }

    @Test
    public void testTorqueBoxYmlWithEnvInEnvVarsAsRACK_ENV() throws Exception {
        MockDeploymentPhaseContext context = setupResourceAsTorqueboxYml( "RACK_ENV-torquebox.yml" );
        MockDeploymentUnit unit = context.getMockDeploymentUnit();
        
        DeprecationLogger logger = mock( DeprecationLogger.class );
        unit.putAttachment( DeprecationLogger.ATTACHMENT_KEY, logger );

        deploy( context );
        verify( logger, never() ).append( anyString() );

        RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
        assertEquals( "biscuit", rubyAppMetaData.getEnvironmentName() );
    }

    @Test
    public void testTorqueBoxYmlWithEnvInEnvVarsAsRAILS_ENV() throws Exception {
        MockDeploymentPhaseContext context = setupResourceAsTorqueboxYml( "RACK_ENV-torquebox.yml" );
        MockDeploymentUnit unit = context.getMockDeploymentUnit();
        
        DeprecationLogger logger = mock( DeprecationLogger.class );
        unit.putAttachment( DeprecationLogger.ATTACHMENT_KEY, logger );

        deploy( context );
        verify( logger, never() ).append( anyString() );

        RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
        assertEquals( "biscuit", rubyAppMetaData.getEnvironmentName() );
    }

}
