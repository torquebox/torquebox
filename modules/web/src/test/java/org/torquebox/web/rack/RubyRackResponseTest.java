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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RubyRackResponseTest extends AbstractRubyTestCase {

    private Ruby ruby;
    private RubyHash headers;

    @Before
    public void setUpRuby() throws Exception {
        this.ruby = createRuby();
        this.headers = createHash( new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;

            {
                put( "header1", "header_value1" );
                put( "header2", "header_value2" );
                put( "header3", "header_value3" );
            }
        } );
    }

    @Test
    public void testHandleStatus() throws Exception {
        IRubyObject rubyRackResponse = createRubyRackResponse( 201, (RubyHash) null, null );

        RackResponse javaRackResponse = new RackResponse( rubyRackResponse );

        HttpServletResponse servletResponse = mock( HttpServletResponse.class );
        javaRackResponse.respond( servletResponse );
        verify( servletResponse ).setStatus( 201 );
    }

    @Test
    public void testHandleHeaders() throws Exception {
        IRubyObject rubyRackResponse = createRubyRackResponse( 200, this.headers, null );

        RackResponse javaRackResponse = new RackResponse( rubyRackResponse );

        HttpServletResponse servletResponse = mock( HttpServletResponse.class );
        javaRackResponse.respond( servletResponse );
        verify( servletResponse ).setStatus( 200 );
        verify( servletResponse ).addHeader( "header1", "header_value1" );
        verify( servletResponse ).addHeader( "header2", "header_value2" );
        verify( servletResponse ).addHeader( "header3", "header_value3" );
    }

    @Test
    public void testHandleMultipleCookies() throws Exception {
        RubyHash cookies = createHash( new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;

            {
                put( "Set-Cookie", "foo1=bar1; path=/\nfoo2=bar2; path=/; expires=Thu, 13-Sep-2012 22:52:32 GMT\nfoo3=bar3; path=/" );
            }
        } );

        IRubyObject rubyRackResponse = createRubyRackResponse( 200, cookies, null );
        RackResponse javaRackResponse = new RackResponse( rubyRackResponse );
        HttpServletResponse servletResponse = mock( HttpServletResponse.class );

        javaRackResponse.respond( servletResponse );
        verify( servletResponse ).setStatus( 200 );
        verify( servletResponse ).addHeader( "Set-Cookie", "foo1=bar1; path=/" );
        verify( servletResponse ).addHeader( "Set-Cookie", "foo2=bar2; path=/; expires=Thu, 13-Sep-2012 22:52:32 GMT" );
        verify( servletResponse ).addHeader( "Set-Cookie", "foo3=bar3; path=/" );
    }

    @Test
    public void testHandleBodyWithoutClose() throws Exception {
        RubyArray body = createBody();

        body.add( JavaEmbedUtils.javaToRuby( this.ruby, "part1" ) );
        body.add( JavaEmbedUtils.javaToRuby( this.ruby, "part2" ) );

        IRubyObject rubyRackResponse = createRubyRackResponse( 200, this.headers, body );

        RackResponse javaRackResponse = new RackResponse( rubyRackResponse );

        HttpServletResponse servletResponse = mock( HttpServletResponse.class );

        ByteArrayOutputStream collector = new ByteArrayOutputStream();
        MockServletOutputStream outputStream = new MockServletOutputStream( collector );
        when( servletResponse.getOutputStream() ).thenReturn( outputStream );

        javaRackResponse.respond( servletResponse );

        String output = new String( collector.toByteArray() );
        assertEquals( "part1part2", output );
    }

    @Test
    public void testHandleBodyWithClose() throws Exception {
        RubyArray body = createCloseableBody();

        body.add( JavaEmbedUtils.javaToRuby( this.ruby, "part1" ) );
        body.add( JavaEmbedUtils.javaToRuby( this.ruby, "part2" ) );

        Boolean closed = (Boolean) JavaEmbedUtils.invokeMethod( this.ruby, body, "closed?", new Object[] {}, Boolean.class );
        assertFalse( closed.booleanValue() );

        IRubyObject rubyRackResponse = createRubyRackResponse( 200, this.headers, body );

        RackResponse javaRackResponse = new RackResponse( rubyRackResponse );

        HttpServletResponse servletResponse = mock( HttpServletResponse.class );
        ByteArrayOutputStream collector = new ByteArrayOutputStream();
        MockServletOutputStream outputStream = new MockServletOutputStream( collector );
        when( servletResponse.getOutputStream() ).thenReturn( outputStream );

        javaRackResponse.respond( servletResponse );

        String output = new String( collector.toByteArray() );
        assertEquals( "part1part2", output );

        closed = (Boolean) JavaEmbedUtils.invokeMethod( this.ruby, body, "closed?", new Object[] {}, Boolean.class );
        assertTrue( closed.booleanValue() );
    }

    protected RubyHash createHash(Map<String, String> in) {
        Map<IRubyObject, IRubyObject> out = new HashMap<IRubyObject, IRubyObject>();
        if (in != null) {
            for (String name : in.keySet()) {
                IRubyObject rubyName = JavaEmbedUtils.javaToRuby( this.ruby, name );
                IRubyObject rubyValue = JavaEmbedUtils.javaToRuby( this.ruby, in.get( name ) );

                out.put( rubyName, rubyValue );
            }
        }
        return new RubyHash( this.ruby, out, null );
    }

    protected IRubyObject createRubyRackResponse(int status, RubyHash headers, IRubyObject body) {
        RubyArray rubyRackResponse = RubyArray.newArray( this.ruby );
        rubyRackResponse.add( status );
        if (headers == null) {
            headers = new RubyHash( this.ruby );
        }
        rubyRackResponse.add( headers );
        if (body == null) {
            body = RubyArray.newArray( this.ruby );
        }
        rubyRackResponse.add( body );
        return rubyRackResponse;
    }

    protected RubyArray createBody() {
        this.ruby.evalScriptlet( "require %q(org/torquebox/web/rack/mock_body)" );
        RubyClass bodyClass = (RubyClass) this.ruby.getClassFromPath( "MockBody" );
        return (RubyArray) JavaEmbedUtils.invokeMethod( this.ruby, bodyClass, "new", new Object[] {}, IRubyObject.class );
    }

    protected RubyArray createCloseableBody() {
        this.ruby.evalScriptlet( "require %q(org/torquebox/web/rack/closeable_mock_body)" );
        RubyClass bodyClass = (RubyClass) this.ruby.getClassFromPath( "CloseableMockBody" );
        return (RubyArray) JavaEmbedUtils.invokeMethod( this.ruby, bodyClass, "new", new Object[] {}, IRubyObject.class );
    }

}
