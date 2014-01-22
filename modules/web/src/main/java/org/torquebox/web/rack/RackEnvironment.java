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

package org.torquebox.web.rack;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyFixnum;
import org.jruby.RubyHash;
import org.jruby.RubyIO;
import org.jruby.RubyModule;
import org.jruby.RubyString;

public class RackEnvironment {

    public RackEnvironment(Ruby ruby, HttpServletRequest request) throws IOException {
        initializeEnv( ruby, request );
    }

    private void initializeEnv(Ruby ruby, HttpServletRequest request) throws IOException {
        this.env = new RubyHash( ruby );

        RubyClass rackChannel = ruby.getModule( "TorqueBox" ).getClass( "RackChannel" );
        this.input = new RackChannel(ruby, rackChannel, request.getInputStream() );
        env.put( RubyString.newString( ruby, "rack.input" ), input );

        env.put( RubyString.newString( ruby, "rack.errors" ), ruby.getGlobalVariables().get( "$stderr" ) );

        RubyArray rackVersion = RubyArray.newArray( ruby );
        rackVersion.add( RubyFixnum.one( ruby ) );
        rackVersion.add( RubyFixnum.one( ruby ) );

        // Don't use request.getPathInfo because that gets decoded by the container
        String pathInfo = request.getRequestURI();
        if (pathInfo.startsWith( request.getContextPath() )) {
            pathInfo = pathInfo.substring( request.getContextPath().length() );
        }
        if (pathInfo.startsWith( request.getServletPath() )) {
            pathInfo = pathInfo.substring( request.getServletPath().length() );
        }

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

        if (log.isTraceEnabled()) {
            log.trace( "Created: " + env.inspect() );
        }
    }

    public RubyHash getEnv() {
        return this.env;
    }

    public void close() {
        //explicitly close the inputstream, but leave the err stream open,
        //as closing that detaches it from the log forever!

        if (this.input != null) {
            try {
                this.input.close();
            } catch (IOException e) {
                log.error( "Error closing rack.input", e );
            }
        }
    }
    
    private static final Logger log = Logger.getLogger( RackEnvironment.class );

    private RubyHash env;
    private RackChannel input;
}
