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

package org.torquebox.messaging.injection;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.injection.analysis.InjectableHandlerRegistry;
import org.torquebox.core.injection.jndi.JNDIInjectableHandler;

public class InjectableHandlerTest {

    private InjectableHandlerRegistry registry;

    protected String readScript(String name) throws IOException {
        InputStream in = getClass().getResourceAsStream( name );
        InputStreamReader reader = new InputStreamReader( in );
        BufferedReader buffered = new BufferedReader( reader );

        String line = null;
        StringBuilder script = new StringBuilder();

        while ((line = buffered.readLine()) != null) {
            script.append( line );
            script.append( "\n" );
        }

        return script.toString();
    }

    @Before
    public void setUpRegistry() {
        this.registry = new InjectableHandlerRegistry();
        this.registry.addInjectableHandler( new JNDIInjectableHandler() );
        this.registry.addInjectableHandler( new TopicInjectableHandler() );
        this.registry.addInjectableHandler( new QueueInjectableHandler() );
    }

    @Test
    public void testGenericAnalysis() throws Exception {
        assertTrue( this.registry.getHandler( "java:/comp/whatever" ) instanceof JNDIInjectableHandler );
        assertTrue( this.registry.getHandler( "/queues/mine" ) instanceof QueueInjectableHandler );
        assertTrue( this.registry.getHandler( "/topics/yours" ) instanceof TopicInjectableHandler );
        assertTrue( this.registry.getHandler( "DLQ" ) instanceof QueueInjectableHandler );
        assertTrue( this.registry.getHandler( "ExpiryQueue" ) instanceof QueueInjectableHandler );
    }



}
