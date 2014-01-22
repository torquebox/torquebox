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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RackEnvironmentTest extends AbstractRubyTestCase {

    private Ruby ruby;

    @Before
    public void setUp() throws Exception {
        this.ruby = createRuby();
        RackChannel.createRackChannelClass( this.ruby );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEnvironment() throws Exception {
        ruby.evalScriptlet( "RACK_ROOT='/test/app'\n" );
        String rackup = "run Proc.new {|env| [200, {'Content-Type' => 'text/html'}, env.inspect]}";
        //RackApplicationImpl rackApp = new RackApplicationImpl( ruby, rackup, VFS.getChild( "/test/path/config.ru" ) );
        //assertNotNull( rackApp );

        final HttpServletRequest servletRequest = mock( HttpServletRequest.class );

        // final InputStream inputStream = new ByteArrayInputStream(
        // "howdy".getBytes() );
        final ServletInputStream inputStream = new MockServletInputStream( new ByteArrayInputStream( "".getBytes() ) );

        when( servletRequest.getInputStream() ).thenReturn( inputStream );
        when( servletRequest.getMethod() ).thenReturn( "GET" );
        when( servletRequest.getRequestURI() ).thenReturn( "/myapp/the_path" );
        when( servletRequest.getContextPath() ).thenReturn( "/myapp" );
        when( servletRequest.getServletPath() ).thenReturn( "/" );
        when( servletRequest.getPathInfo() ).thenReturn( "the_path" );
        when( servletRequest.getQueryString() ).thenReturn( "cheese=cheddar&bob=mcwhirter" );
        when( servletRequest.getServerName() ).thenReturn( "torquebox.org" );
        when( servletRequest.getScheme() ).thenReturn( "https" );
        when( servletRequest.getServerPort() ).thenReturn( 8080 );
        when( servletRequest.getContentType() ).thenReturn( "text/html" );
        when( servletRequest.getContentLength() ).thenReturn( 0 );
        when( servletRequest.getRemoteAddr() ).thenReturn( "10.42.42.42" );
        when( servletRequest.getHeaderNames() ).thenReturn( enumeration( "header1", "header2" ) );
        when( servletRequest.getHeader( "header1" ) ).thenReturn( "header_value1" );
        when( servletRequest.getHeader( "header2" ) ).thenReturn( "header_value2" );

        // IRubyObject rubyEnv = (IRubyObject)
        // rackApp.createEnvironment(servletContext, servletRequest);
        // assertNotNull(rubyEnv);

        // Map<String, Object> javaEnv = (Map<String, Object>)
        // rubyEnv.toJava(Map.class);
        RackEnvironment env = new RackEnvironment( ruby, servletRequest );
        RubyHash envHash = env.getEnv();
        assertNotNull( envHash );

        assertEquals( "GET", envHash.get( "REQUEST_METHOD" ) );
        assertEquals( "/myapp/the_path", envHash.get( "REQUEST_URI" ) );
        assertEquals( "cheese=cheddar&bob=mcwhirter", envHash.get( "QUERY_STRING" ) );
        assertEquals( "torquebox.org", envHash.get( "SERVER_NAME" ) );
        assertEquals( "https", envHash.get( "rack.url_scheme" ) );
        assertEquals( "on", envHash.get( "HTTPS" ) );
        assertEquals( "8080", envHash.get( "SERVER_PORT" ) );
        assertEquals( "text/html", envHash.get( "CONTENT_TYPE" ) );
        assertEquals( 0L, envHash.get( "CONTENT_LENGTH" ) );
        assertEquals( "10.42.42.42", envHash.get( "REMOTE_ADDR" ) );

        assertEquals( "header_value1", envHash.get( "HTTP_HEADER1" ) );
        assertEquals( "header_value2", envHash.get( "HTTP_HEADER2" ) );

        assertNotNull( envHash.get( "rack.input" ) );
        assertNotNull( envHash.get( "rack.errors" ) );
        assertSame( servletRequest, envHash.get( "servlet_request" ) );
        assertSame( servletRequest, envHash.get( "java.servlet_request" ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAnotherEnvironment() throws Exception {
        ruby.evalScriptlet( "RACK_ROOT='/test/app'\n" );
        String rackup = "run Proc.new {|env| [200, {'Content-Type' => 'text/html'}, env.inspect]}";
        //RackApplicationImpl rackApp = new RackApplicationImpl( ruby, rackup, VFS.getChild( "/test/path/config.ru" ) );
        //assertNotNull( rackApp );

        final HttpServletRequest servletRequest = mock( HttpServletRequest.class );

        // final InputStream inputStream = new ByteArrayInputStream(
        // "howdy".getBytes() );
        final ServletInputStream inputStream = new MockServletInputStream( new ByteArrayInputStream( "".getBytes() ) );

        when( servletRequest.getInputStream() ).thenReturn( inputStream );
        when( servletRequest.getMethod() ).thenReturn( "GET" );
        when( servletRequest.getRequestURI() ).thenReturn( "/myapp/the_path" );
        when( servletRequest.getContextPath() ).thenReturn( "/myapp" );
        when( servletRequest.getServletPath() ).thenReturn( "/" );
        when( servletRequest.getPathInfo() ).thenReturn( "the_path" );
        when( servletRequest.getQueryString() ).thenReturn( "cheese=cheddar&bob=mcwhirter" );
        when( servletRequest.getServerName() ).thenReturn( "torquebox.org" );
        when( servletRequest.getScheme() ).thenReturn( "http" );
        when( servletRequest.getServerPort() ).thenReturn( 8080 );
        when( servletRequest.getContentType() ).thenReturn( null );
        when( servletRequest.getContentLength() ).thenReturn( 0 );
        when( servletRequest.getRemoteAddr() ).thenReturn( "10.42.42.42" );
        when( servletRequest.getHeaderNames() ).thenReturn( enumeration( "header1", "header2" ) );
        when( servletRequest.getHeader( "header1" ) ).thenReturn( "header_value1" );
        when( servletRequest.getHeader( "header2" ) ).thenReturn( "header_value2" );

        // IRubyObject rubyEnv = (IRubyObject)
        // rackApp.createEnvironment(servletContext, servletRequest);
        // assertNotNull(rubyEnv);

        // Map<String, Object> javaEnv = (Map<String, Object>)
        // rubyEnv.toJava(Map.class);
        RackEnvironment env = new RackEnvironment( ruby, servletRequest );
        RubyHash envHash = env.getEnv();
        assertNotNull( envHash );

        assertEquals( "GET", envHash.get( "REQUEST_METHOD" ) );
        assertEquals( "/myapp/the_path", envHash.get( "REQUEST_URI" ) );
        assertEquals( "cheese=cheddar&bob=mcwhirter", envHash.get( "QUERY_STRING" ) );
        assertEquals( "torquebox.org", envHash.get( "SERVER_NAME" ) );
        assertEquals( "http", envHash.get( "rack.url_scheme" ) );
        assertNull( envHash.get( "HTTPS" ) );
        assertEquals( "8080", envHash.get( "SERVER_PORT" ) );
        assertNull( envHash.get( "CONTENT_TYPE" ) );
        assertEquals( 0L, envHash.get( "CONTENT_LENGTH" ) );
        assertEquals( "10.42.42.42", envHash.get( "REMOTE_ADDR" ) );

        assertEquals( "header_value1", envHash.get( "HTTP_HEADER1" ) );
        assertEquals( "header_value2", envHash.get( "HTTP_HEADER2" ) );

        assertNotNull( envHash.get( "rack.input" ) );
        assertNotNull( envHash.get( "rack.errors" ) );
        assertSame( servletRequest, envHash.get( "servlet_request" ) );
        assertSame( servletRequest, envHash.get( "java.servlet_request" ) );
    }

    @Test
    public void testMissingContentLength() throws Exception {

        final HttpServletRequest servletRequest = mock( HttpServletRequest.class );
        final ServletInputStream inputStream = new MockServletInputStream( new ByteArrayInputStream( "".getBytes() ) );

        when( servletRequest.getInputStream() ).thenReturn( inputStream );
        when( servletRequest.getContentLength() ).thenReturn( -1 );
        when( servletRequest.getRequestURI() ).thenReturn( "/myapp/the_path" );
        when( servletRequest.getContextPath() ).thenReturn( "/myapp" );
        when( servletRequest.getServletPath() ).thenReturn( "/" );

        RackEnvironment env = new RackEnvironment( ruby, servletRequest );
        RubyHash envHash = env.getEnv();
        assertNotNull( envHash );
        assertNull( envHash.get( "CONTENT_LENGTH" ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLeakingRubyFilenoMap() throws Exception {
        final HttpServletRequest servletRequest = mock( HttpServletRequest.class );
        final ServletInputStream inputStream = new MockServletInputStream( new ByteArrayInputStream( "".getBytes() ) );

        when( servletRequest.getInputStream() ).thenReturn( inputStream );
        when( servletRequest.getRequestURI() ).thenReturn( "/myapp/the_path" );
        when( servletRequest.getContextPath() ).thenReturn( "/myapp" );
        when( servletRequest.getServletPath() ).thenReturn( "/" );

        // This is hacky to do via reflection but we've had a leak in this specific HashMap
        // and need to ensure it's fixed
        Field filenoMapField = ruby.getClass().getDeclaredField( "filenoIntExtMap" );
        filenoMapField.setAccessible( true );
        Map<Integer, Integer> filenoIntExtMap = (Map<Integer, Integer>) filenoMapField.get( ruby );

        int startingSize = filenoIntExtMap.size();
        new RackEnvironment( ruby, servletRequest );
        int sizeAfterCreatingEnv = filenoIntExtMap.size();

        assertEquals( startingSize, sizeAfterCreatingEnv );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Enumeration enumeration(Object... values) {
        Vector v = new Vector();
        for (Object each : values) {
            v.add( each );
        }
        return v.elements();
    }
}
