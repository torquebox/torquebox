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

package org.torquebox.messaging.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.as.server.deployment.DeploymentException;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.app.processors.AppKnobYamlParsingProcessor;
import org.torquebox.core.processors.TorqueBoxYamlParsingProcessor;
import org.torquebox.messaging.MessageProcessorMetaData;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class MessagingYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUpDeployer() throws Throwable {
        clearDeployers();
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new MessagingYamlParsingProcessor() );
    }

    @Test
    public void testSettingDefaultMessageEncoding() throws Exception {
        MockDeploymentPhaseContext context = setupResourceAsTorqueboxYml( "messaging-with-default-encoding.yml" );
        MockDeploymentUnit unit = context.getMockDeploymentUnit();
        RubyAppMetaData appMetaData = new RubyAppMetaData( "app-name" );
        appMetaData.setRoot( new File( "/home/mrplow" ) );
        Map<String, String> env = new HashMap<String, String>();
        appMetaData.setEnvironmentVariables( env );
        appMetaData.attachTo( unit );
        deploy( context );

        assertEquals( "marshal", env.get( "DEFAULT_MESSAGE_ENCODING" ) );

        List<MessageProcessorMetaData> procMetaData = unit.getAttachmentList( MessageProcessorMetaData.ATTACHMENTS_KEY );
        assertEquals( 1, procMetaData.size() );
        MessageProcessorMetaData metaData = procMetaData.iterator().next();
        assertNotNull( metaData );
        assertEquals( "MyClass", metaData.getRubyClassName() );
        assertEquals( "/topics/foo", metaData.getDestinationName() );
    }

    @Test
    public void testEmptyMessagingConfig() throws Exception {
        List<MessageProcessorMetaData> allMetaData = getMetaData( "empty.yml" );
        assertTrue( allMetaData.isEmpty() );
    }

    @Test(expected = DeploymentException.class)
    public void testJunkMessagingConfig() throws Exception {
        List<MessageProcessorMetaData> allMetaData = getMetaData( "junk-messaging.yml" );
        assertTrue( allMetaData.isEmpty() );
    }

    @Test
    public void testSingleMessagingConfig() throws Exception {
        List<MessageProcessorMetaData> allMetaData = getMetaData( "single-messaging.yml" );
        assertEquals( 1, allMetaData.size() );

        MessageProcessorMetaData metaData = allMetaData.iterator().next();
        assertNotNull( metaData );
        assertEquals( "MyClass", metaData.getRubyClassName() );
        assertEquals( "/topics/foo", metaData.getDestinationName() );
        assertEquals( "mySelector", metaData.getMessageSelector() );
        assertEquals( "toast", metaData.getRubyConfig().get( "a" ) );
        assertEquals( new Integer( 2 ), metaData.getConcurrency() );
        assertFalse( metaData.isStopped() );
    }

    @Test
    public void testMappingsFromAllConfigStyles() throws Exception {
        List<MessageProcessorMetaData> allMetaData = getMetaData( "messaging.yml" );
        assertEquals( 7, allMetaData.size() );
        assertEquals( 1, filter( allMetaData, "/simple" ).size() );
        assertEquals( 3, filter( allMetaData, "/array" ).size() );
        assertEquals( 3, filter( allMetaData, "/hash" ).size() );
        assertNotNull( find( allMetaData, "/simple", "Simple" ) );
        assertNotNull( find( allMetaData, "/array", "One" ) );
        assertNotNull( find( allMetaData, "/array", "Two" ) );
        assertNotNull( find( allMetaData, "/array", "Three" ) );
        assertNotNull( find( allMetaData, "/hash", "A" ) );
        assertNotNull( find( allMetaData, "/hash", "B" ) );
        assertNotNull( find( allMetaData, "/hash", "Two" ) );
    }

    @Test
    public void testConfigOptionsForArray() throws Exception {
        List<MessageProcessorMetaData> allMetaData = getMetaData( "messaging.yml" );
        MessageProcessorMetaData metadata = find( allMetaData, "/array", "Two" );
        assertEquals( "x > 18", metadata.getMessageSelector() );
        assertFalse( metadata.isXAEnabled() );
        Map<String, Object> config = metadata.getRubyConfig();
        assertEquals( "ex", config.get( "x" ) );
        assertEquals( "why", config.get( "y" ) );
        assertTrue( isUnconfigured( find( allMetaData, "/array", "One" ) ) );
        assertTrue( isUnconfigured( find( allMetaData, "/array", "Three" ) ) );
    }

    @Test
    public void testConfigOptionsForHash() throws Exception {
        List<MessageProcessorMetaData> allMetaData = getMetaData( "messaging.yml" );
        MessageProcessorMetaData metadata = find( allMetaData, "/hash", "B" );
        assertEquals( "y < 18", metadata.getMessageSelector() );
        assertTrue( metadata.isXAEnabled() );
        Map<String, Object> config = metadata.getRubyConfig();
        assertEquals( "ache", config.get( "h" ) );
        assertEquals( "eye", config.get( "i" ) );
        assertEquals( new Integer( 3 ), metadata.getConcurrency() );
        assertTrue( isUnconfigured( find( allMetaData, "/hash", "A" ) ) );
    }

    @Test
    public void testMergedMap() throws Exception {
        List<MessageProcessorMetaData> allMetaData = getMetaData( "messaging.yml" );
        MessageProcessorMetaData metadata = find( allMetaData, "/hash", "Two" );
        assertEquals( "x > 18", metadata.getMessageSelector() );
        assertFalse( metadata.isXAEnabled() );
        Map<String, Object> config = metadata.getRubyConfig();
        assertEquals( "ex", config.get( "x" ) );
        assertEquals( "why", config.get( "y" ) );
    }

    @Test
    public void testDefaultConcurrency() throws Exception {
        List<MessageProcessorMetaData> allMetaData = getMetaData( "messaging.yml" );
        MessageProcessorMetaData metadata = find( allMetaData, "/hash", "A" );
        assertEquals( new Integer( 1 ), metadata.getConcurrency() );
    }

    @Test
    public void testStopped() throws Exception {
        List<MessageProcessorMetaData> allMetaData = getMetaData( "messaging-stopped.yml" );
        MessageProcessorMetaData metadata = find( allMetaData, "/topics/foo", "MyClass" );
        assertTrue( metadata.isStopped() );
    }

    private List<MessageProcessorMetaData> getMetaData(String filename) throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( filename );

        return unit.getAttachmentList( MessageProcessorMetaData.ATTACHMENTS_KEY );
    }

    private List<MessageProcessorMetaData> filter(List<MessageProcessorMetaData> metadata, String destination) {
        List<MessageProcessorMetaData> results = new ArrayList<MessageProcessorMetaData>();
        for (MessageProcessorMetaData md : metadata) {
            if (destination.equals( md.getDestinationName() )) {
                results.add( md );
            }
        }
        return results;
    }

    private MessageProcessorMetaData find(List<MessageProcessorMetaData> metadata, String destination, String handler) {
        for (MessageProcessorMetaData md : metadata) {
            if (destination.equals( md.getDestinationName() ) && handler.equals( md.getRubyClassName() )) {
                return md;
            }
        }
        return null;
    }

    private boolean isUnconfigured(MessageProcessorMetaData metadata) {
        return null == metadata.getMessageSelector() && null == metadata.getRubyConfig();
    }

}
