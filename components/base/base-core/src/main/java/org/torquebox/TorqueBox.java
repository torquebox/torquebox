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

package org.torquebox;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.logging.Logger;

public class TorqueBox {

    private static final Logger log = Logger.getLogger( TorqueBox.class );

    private Properties properties = new Properties();

    public TorqueBox() {

    }

    public void create() throws IOException {
        InputStream propsStream = getClass().getResourceAsStream( "torquebox.properties" );
        if (propsStream != null) {
            try {
                this.properties.load( propsStream );
            } finally {
                propsStream.close();
            }
        }
    }

    protected String getVersion() {
        return this.properties.getProperty( "version", "(unknown)" );
    }

    protected String getRevision() {
        return this.properties.getProperty( "build.revision", "(unknown)" );
    }

    protected String getBuildNumber() {
        return this.properties.getProperty( "build.number" );
    }
    
    protected String getBuildUser() {
        return this.properties.getProperty( "build.user" );
    }

    public void start() {
        log.info( "Welcome to TorqueBox AS - http://torquebox.org/" );
        log.info( "  version...... " + getVersion() );
        String buildNo = getBuildNumber();
        if (buildNo != null && ! buildNo.trim().equals( "" )) {
            log.info( "  build........ " + getBuildNumber() );
        } else if ( getVersion().contains(  "SNAPSHOT"  ) ) {
            log.info( "  build........ development (" + getBuildUser() + ")" );
        } else {
            log.info( "  build........ official" );
        }
        log.info( "  revision..... " + getRevision() );
        log.info( "  jruby.home... " + System.getProperty( "jruby.home" ) );
    }

    public void stop() {

    }

}
