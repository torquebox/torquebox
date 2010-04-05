package org.torquebox.rack.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
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

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

public class RubyRackResponseTest extends AbstractRubyTestCase {

	private Ruby ruby;
	private Map<String, String> headers;

	@Before
	public void setUpRuby() throws Exception {
		this.ruby = createRuby();
		this.headers = new HashMap<String, String>() {
			{
				put("header1", "header_value1");
				put("header2", "header_value2");
				put("header3", "header_value3");
			}
		};
	}

	@Test
	public void testHandleStatus() throws Exception {
		IRubyObject rubyRackResponse = createRubyRackResponse(201, (RubyHash) null, null);

		RubyRackResponse javaRackResponse = new RubyRackResponse(rubyRackResponse);

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		javaRackResponse.respond(servletResponse);
		verify(servletResponse).setStatus(201);
	}

	@Test
	public void testHandleHeaders() throws Exception {
		IRubyObject rubyRackResponse = createRubyRackResponse(200, this.headers, null);

		RubyRackResponse javaRackResponse = new RubyRackResponse(rubyRackResponse);

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		javaRackResponse.respond(servletResponse);
		verify(servletResponse).setStatus(200);
		verify(servletResponse).addHeader("header1", "header_value1");
		verify(servletResponse).addHeader("header2", "header_value2");
		verify(servletResponse).addHeader("header3", "header_value3");
	}

	@Test
	public void testHandleBodyWithoutClose() throws Exception {
		RubyArray body = createBody();
		
		body.add( JavaEmbedUtils.javaToRuby( this.ruby, "part1" ) );
		body.add( JavaEmbedUtils.javaToRuby( this.ruby, "part2" ) );
		
		IRubyObject rubyRackResponse = createRubyRackResponse(200, this.headers, body);

		RubyRackResponse javaRackResponse = new RubyRackResponse(rubyRackResponse);

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		
		ByteArrayOutputStream collector = new ByteArrayOutputStream();
		MockServletOutputStream outputStream = new MockServletOutputStream(collector);
		when( servletResponse.getOutputStream() ).thenReturn( outputStream );
		
		javaRackResponse.respond(servletResponse);
		
		String output = new String( collector.toByteArray() );
		assertEquals( "part1part2", output );
	}

	@Test
	public void testHandleBodyWithClose() throws Exception {
		RubyArray body = createCloseableBody();
		
		body.add( JavaEmbedUtils.javaToRuby( this.ruby, "part1" ) );
		body.add( JavaEmbedUtils.javaToRuby( this.ruby, "part2" ) );
		
		Boolean closed = (Boolean) JavaEmbedUtils.invokeMethod(this.ruby, body, "closed?", new Object[] {}, Boolean.class);
		assertFalse(closed.booleanValue());
		
		IRubyObject rubyRackResponse = createRubyRackResponse(200, this.headers, body);

		RubyRackResponse javaRackResponse = new RubyRackResponse(rubyRackResponse);

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		ByteArrayOutputStream collector = new ByteArrayOutputStream();
		MockServletOutputStream outputStream = new MockServletOutputStream(collector);
		when( servletResponse.getOutputStream() ).thenReturn( outputStream );
		
		javaRackResponse.respond(servletResponse);
		
		String output = new String( collector.toByteArray() );
		assertEquals( "part1part2", output );

		closed = (Boolean) JavaEmbedUtils.invokeMethod(this.ruby, body, "closed?", new Object[] {}, Boolean.class);
		assertTrue(closed.booleanValue());
	}

	protected IRubyObject createRubyRackResponse(int status, Map<String, String> headers, IRubyObject body) {
		Map<IRubyObject, IRubyObject> rubyHeaders = new HashMap<IRubyObject, IRubyObject>();
		if (headers != null) {
			for (String name : headers.keySet()) {
				IRubyObject rubyName = JavaEmbedUtils.javaToRuby(this.ruby, name);
				IRubyObject rubyValue = JavaEmbedUtils.javaToRuby(this.ruby, headers.get(name));

				rubyHeaders.put(rubyName, rubyValue);
			}
		}
		return createRubyRackResponse(status, new RubyHash(this.ruby, rubyHeaders, null), body);
	}

	protected IRubyObject createRubyRackResponse(int status, RubyHash headers, IRubyObject body) {
		RubyArray rubyRackResponse = RubyArray.newArray(this.ruby);
		rubyRackResponse.add(status);
		if (headers == null) {
			headers = new RubyHash(this.ruby);
		}
		rubyRackResponse.add(headers);
		if (body == null) {
			body = RubyArray.newArray(this.ruby);
		}
		rubyRackResponse.add(body);
		return rubyRackResponse;
	}

	protected RubyArray createBody() {
		this.ruby.evalScriptlet("require %q(org/torquebox/rack/core/mock_body)");
		RubyClass bodyClass = (RubyClass) this.ruby.getClassFromPath("MockBody");
		return (RubyArray) JavaEmbedUtils.invokeMethod(this.ruby, bodyClass, "new", new Object[] {}, IRubyObject.class);
	}

	protected RubyArray createCloseableBody() {
		this.ruby.evalScriptlet("require %q(org/torquebox/rack/core/closeable_mock_body)");
		RubyClass bodyClass = (RubyClass) this.ruby.getClassFromPath("CloseableMockBody");
		return (RubyArray) JavaEmbedUtils.invokeMethod(this.ruby, bodyClass, "new", new Object[] {}, IRubyObject.class);
	}

}
