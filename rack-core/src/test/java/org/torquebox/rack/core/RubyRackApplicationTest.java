package org.torquebox.rack.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Ignore;
import org.junit.Test;
import org.torquebox.test.ruby.AbstractRubyTestCase;

import static org.junit.Assert.*;

public class RubyRackApplicationTest extends AbstractRubyTestCase {

	Mockery context = new JUnit4Mockery();

	@Ignore
	@Test
	public void testConstruct() throws Exception {
		Ruby ruby = createRuby();

		String rackup = "run Proc.new {|env| [200, {'Content-Type' => 'text/html'}, env.inspect]}";
		RubyRackApplication rackApp = new RubyRackApplication(ruby, rackup);
		IRubyObject rubyApp = rackApp.getRubyApplication();
		assertNotNil(rubyApp);
	}

	@Test
	public void testEnvironment() throws Exception {
		Ruby ruby = createRuby();
		String rackup = "run Proc.new {|env| [200, {'Content-Type' => 'text/html'}, env.inspect]}";
		RubyRackApplication rackApp = new RubyRackApplication(ruby, rackup);

		final ServletContext servletContext = context.mock(ServletContext.class);
		final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);

		// final InputStream inputStream = new ByteArrayInputStream(
		// "howdy".getBytes() );
		final InputStream inputStream = new MockServletInputStream(new ByteArrayInputStream("".getBytes()));

		context.checking(new Expectations() {
			{
				oneOf(servletRequest).getInputStream();
				will(returnValue(inputStream));

				oneOf(servletRequest).getMethod();
				will(returnValue("GET"));

				oneOf(servletRequest).getContextPath();
				will(returnValue("/"));

				oneOf(servletRequest).getServletPath();
				will(returnValue("/myapp"));

				oneOf(servletRequest).getPathInfo();
				will(returnValue("the_path"));

				oneOf(servletRequest).getQueryString();
				will(returnValue("cheese=cheddar&bob=mcwhirter"));

				oneOf(servletRequest).getServerName();
				will(returnValue("torquebox.org"));

				oneOf(servletRequest).getServerPort();
				will(returnValue(8080));

				oneOf(servletRequest).getContentType();
				will(returnValue("text/html"));

				oneOf(servletRequest).getContentLength();
				will(returnValue(0));

				oneOf(servletRequest).getRemoteAddr();
				will(returnValue("10.42.42.42"));

				oneOf(servletRequest).getHeaderNames();
				will(returnEnumeration(new ArrayList<String>() {
					{
						add("header1");
						add("header2");
					}
				}));
				
				oneOf(servletRequest).getHeader( "header1" );
				will( returnValue( "header_value1" ) );
				
				oneOf(servletRequest).getHeader( "header2" );
				will( returnValue( "header_value2" ) );
			}
		});

		Object environment = rackApp.createEnvironment(servletContext, servletRequest);

		System.err.println("env=" + environment);

		assertNotNull(environment);

	}
}
