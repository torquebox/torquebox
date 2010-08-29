package org.torquebox.rack.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.jboss.vfs.VFS;
import org.jruby.Ruby;
import org.junit.Test;
import org.torquebox.rack.spi.RackEnvironment;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RackEnvironmentImplTest extends AbstractRubyTestCase {

	@SuppressWarnings("unchecked")
	@Test
	public void testEnvironment() throws Exception {
		Ruby ruby = createRuby();
		ruby.evalScriptlet("RACK_ROOT='/test/app'\n");
		String rackup = "run Proc.new {|env| [200, {'Content-Type' => 'text/html'}, env.inspect]}";
		RackApplicationImpl rackApp = new RackApplicationImpl(ruby, rackup, VFS.getChild("/test/path/config.ru"));

		final ServletContext servletContext = mock(ServletContext.class);
		final HttpServletRequest servletRequest = mock(HttpServletRequest.class);

		// final InputStream inputStream = new ByteArrayInputStream(
		// "howdy".getBytes() );
		final ServletInputStream inputStream = new MockServletInputStream(new ByteArrayInputStream("".getBytes()));

		when(servletRequest.getInputStream()).thenReturn(inputStream);
		when(servletRequest.getMethod()).thenReturn("GET");
		when(servletRequest.getContextPath()).thenReturn("/");
		when(servletRequest.getServletPath()).thenReturn("myapp/");
		when(servletRequest.getPathInfo()).thenReturn("the_path");
		when(servletRequest.getQueryString()).thenReturn("cheese=cheddar&bob=mcwhirter");
		when(servletRequest.getServerName()).thenReturn("torquebox.org");
		when(servletRequest.getScheme()).thenReturn("https");
		when(servletRequest.getServerPort()).thenReturn(8080);
		when(servletRequest.getContentType()).thenReturn("text/html");
		when(servletRequest.getContentLength()).thenReturn(0);
		when(servletRequest.getRemoteAddr()).thenReturn("10.42.42.42");
		when(servletRequest.getHeaderNames()).thenReturn(enumeration("header1", "header2"));
		when(servletRequest.getHeader("header1")).thenReturn("header_value1");
		when(servletRequest.getHeader("header2")).thenReturn("header_value2");

		//IRubyObject rubyEnv = (IRubyObject) rackApp.createEnvironment(servletContext, servletRequest);
		//assertNotNull(rubyEnv);

		//Map<String, Object> javaEnv = (Map<String, Object>) rubyEnv.toJava(Map.class);
		RackEnvironment env = new RackEnvironmentImpl( ruby, servletContext, servletRequest );
		Map<String, Object> envMap = env.getEnv();
		assertNotNull(envMap);

		assertEquals("GET", envMap.get("REQUEST_METHOD"));
		assertEquals("/myapp/the_path", envMap.get("REQUEST_URI"));
		assertEquals("cheese=cheddar&bob=mcwhirter", envMap.get("QUERY_STRING"));
		assertEquals("torquebox.org", envMap.get("SERVER_NAME"));
		assertEquals("https", envMap.get("rack.url_scheme"));
		assertEquals(8080, envMap.get("SERVER_PORT"));
		assertEquals("text/html", envMap.get("CONTENT_TYPE"));
		assertEquals(0, envMap.get("CONTENT_LENGTH"));
		assertEquals("10.42.42.42", envMap.get("REMOTE_ADDR"));

		assertEquals("header_value1", envMap.get("HTTP_HEADER1"));
		assertEquals("header_value2", envMap.get("HTTP_HEADER2"));

		assertNotNull(envMap.get("rack.input"));
		assertNotNull(envMap.get("rack.errors"));
		assertSame(servletRequest, envMap.get("servlet_request"));
		assertSame(servletRequest, envMap.get("java.servlet_request"));

	}

	@SuppressWarnings("unchecked")
	protected Enumeration enumeration(Object... values) {
		Vector v = new Vector();
		for (Object each : values) {
			v.add(each);
		}
		return v.elements();
	}
}