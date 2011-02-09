package org.torquebox.rack.core;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jboss.logging.Logger;

public class DebugValve extends ValveBase {
    private static final Logger log = Logger.getLogger( DebugValve.class );

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        log.debug( "DEBUG: " + getContainer() );
        log.debug( "Request=" + request );
        log.debug( "Response=" + response );
        getNext().invoke( request, response );
    }

}
