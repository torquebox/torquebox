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

package org.torquebox.integration.arquillian.alacarte;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

@Run(RunModeType.AS_CLIENT)
public class ServicesTest extends AbstractIntegrationTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "alacarte/services-knob.yml" );
    }

    private File file;

    @Before
    public void setUp() {
        this.file = new File( System.getProperty( "basedir" ) + "/target/touchfile.txt" );
        this.file.delete();
    }

    @Test
    public void testDeployment() throws Exception {
        Set<String> seenValues = new HashSet<String>();

        for (int i = 0; i < 10; ++i) {
            Thread.sleep( 1000 );
            if (this.file.exists()) {
                BufferedReader reader = new BufferedReader( new FileReader( this.file ) );

                StringBuffer buffer = new StringBuffer();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    buffer.append( line.trim() );
                }
                String value = buffer.toString().trim();
                
                seenValues.add(  value  );
            }
        }
        
        assertFalse( seenValues.isEmpty() );
        assertTrue( seenValues.size() > 5 );
    }

}
