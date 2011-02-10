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
        return this.properties.getProperty( "build.number", "(unknown)" );
    }

    public void start() {
        log.info( "Welcome to TorqueBox AS - http://torquebox.org/" );
        log.info( "  version.... " + getVersion() );
        log.info( "  build...... " + getBuildNumber() );
        log.info( "  revision... " + getRevision() );
    }

    public void stop() {

    }

}
