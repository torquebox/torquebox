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
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.projectodd.polyglot.core.util.DeprecationLogger;
import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.torquebox.core.TorqueBoxMetaData;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.processors.TorqueBoxYamlParsingProcessor;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class ApplicationYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {
    
    @Before
    public void setUp() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new ApplicationYamlParsingProcessor() );
        appendDeployer( new RubyApplicationDefaultsProcessor() );
    }

    @Test
    public void testSimpleTorqueBoxYml() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "simple-torquebox.yml" );
        
        TorqueBoxMetaData metaData = unit.getAttachment( TorqueBoxMetaData.ATTACHMENT_KEY );
        assertNotNull( metaData );

        RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
        assertNotNull( rubyAppMetaData );
        
        assertEquals( RubyAppMetaData.DEFAULT_ENVIRONMENT_NAME, rubyAppMetaData.getEnvironmentName() );
        
        assertEquals( "torquebox", rubyAppMetaData.getApplicationName() );
    }

    @Test
    public void testTorqueBoxYmlWithAppEnv() throws Exception {
        MockDeploymentPhaseContext context = setupResourceAsTorqueboxYml( "app-env-torquebox.yml" );
        MockDeploymentUnit unit = context.getMockDeploymentUnit();
        
        DeprecationLogger logger = mock( DeprecationLogger.class );
        unit.putAttachment( DeprecationLogger.ATTACHMENT_KEY, logger );

        deploy( context );
        verify( logger ).append( anyString() );

        RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
        assertEquals( "biscuit", rubyAppMetaData.getEnvironmentName() );
    }

}
