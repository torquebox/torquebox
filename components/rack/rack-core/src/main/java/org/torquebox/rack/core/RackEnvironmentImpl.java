package org.torquebox.rack.core;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyFixnum;
import org.jruby.RubyHash;
import org.jruby.RubyIO;
import org.jruby.RubyString;
import org.jruby.util.io.STDIO;
import org.torquebox.rack.spi.RackEnvironment;

public class RackEnvironmentImpl implements RackEnvironment {

    private static final Logger log = Logger.getLogger( RackEnvironmentImpl.class );

    private RubyHash env;
    private RubyIO input;
    private RubyIO errors;

    public RackEnvironmentImpl(Ruby ruby, ServletContext servletContext, HttpServletRequest request) throws IOException {
        initializeEnv( ruby, servletContext, request );
    }

    private void initializeEnv(Ruby ruby, ServletContext servletContext, HttpServletRequest request) throws IOException {
        this.env = new RubyHash( ruby );

        this.input = new RubyIO( ruby, request.getInputStream() );
        env.put( RubyString.newString( ruby, "rack.input" ), input );

        this.errors = new RubyIO( ruby, STDIO.ERR );
        env.put( RubyString.newString( ruby, "rack.errors" ), errors );

        RubyArray rackVersion = RubyArray.newArray( ruby );
        rackVersion.add( RubyFixnum.one( ruby ) );
        rackVersion.add( RubyFixnum.one( ruby ) );

        String pathInfo = (request.getPathInfo() == null ? "" : request.getPathInfo());

        env.put( "REQUEST_METHOD", request.getMethod() );
        env.put( "SCRIPT_NAME", request.getContextPath() + request.getServletPath() );
        env.put( "PATH_INFO", pathInfo );
        env.put( "QUERY_STRING", request.getQueryString() == null ? "" : request.getQueryString() );
        env.put( "SERVER_NAME", request.getServerName() );
        env.put( "SERVER_PORT", request.getServerPort() + "" );
        env.put( "CONTENT_TYPE", request.getContentType() );
        env.put( "REQUEST_URI", request.getContextPath() + request.getServletPath() + pathInfo );
        env.put( "REMOTE_ADDR", request.getRemoteAddr() );
        env.put( "rack.url_scheme", request.getScheme() );
        env.put( "rack.version", rackVersion );
        env.put( "rack.multithread", true );
        env.put( "rack.multiprocess", true );
        env.put( "rack.run_once", false );

        if (request.getContentLength() >= 0) {
            env.put( "CONTENT_LENGTH", request.getContentLength() );
        }

        if ("https".equals( request.getScheme() )) {
            env.put( "HTTPS", "on" );
        }

        if (request.getHeaderNames() != null) {
            for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
                String headerName = headerNames.nextElement();
                String envName = "HTTP_" + headerName.toUpperCase().replace( '-', '_' );

                String value = request.getHeader( headerName );

                env.put( RubyString.newString( ruby, envName ), value );
            }
        }

        env.put( "servlet_request", request );
        env.put( "java.servlet_request", request );

        log.debug( "Created environment: " + env.inspect() );
    }

    public RubyHash getEnv() {
        return this.env;
    }

    public void close() {

        if (this.input != null) {
            if (!this.input.isClosed()) {
                this.input.close();
            }
        }

        if (this.errors != null) {
            if (!this.errors.isClosed()) {
                this.errors.close();
            }
        }
    }

}
