/* Copyright 2009 Red Hat, Inc. */
package org.torquebox.rack.core;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
//import javax.servlet.sip.SipServlet;
//import javax.servlet.sip.SipServletMessage;
//import javax.servlet.sip.SipServletRequest;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyIO;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.common.util.StringUtils;
import org.torquebox.rack.spi.RackApplication;
import org.torquebox.rack.spi.RackResponse;

/**
 * Concrete implementation of {@link RackApplication}.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class RackApplicationImpl implements RackApplication {

	/** Log. */
	private static final Logger log = Logger.getLogger(RackApplicationImpl.class);

	/** Empty object array for ruby invocation. */
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};

	/** Ruby object of the app. */
	private IRubyObject rubyApp;

	/**
	 * Construct.
	 * 
	 * @param ruby The Ruby interpreter to use for this application.
	 * @param rackUpScript The rackup script.
	 */
	public RackApplicationImpl(Ruby ruby, String rackUpScript) {
		this.rubyApp = rackUp(ruby, rackUpScript);
	}

	/**
	 * Perform rackup.
	 * 
	 * @param script The rackup script.
	 */
	private IRubyObject rackUp(Ruby ruby, String script) {
		String fullScript = "require %q(rack/builder)\n" 
			//+ "require %q(org/torquebox/ruby/enterprise/web/rack/middleware/reloader)\n" 
			+ "Rack::Builder.new{(\n" + script + "\n)}.to_app";

		return ruby.executeScript(fullScript, "RubyRackApplication/rackup.rb");
	}
	
	protected IRubyObject getRubyApplication() {
		return this.rubyApp;
	}
	
	public Ruby getRuby() {
		return this.rubyApp.getRuntime();
	}

	/**
	 * Create the request environment ({@code env}) for request handling.
	 * 
	 * @param context The servlet context.
	 * @param request The servlet request.
	 * 
	 * @return The Ruby Rack request environment.
	 */
	public Object createEnvironment(ServletContext context, HttpServletRequest request) throws Exception {
		Ruby ruby = rubyApp.getRuntime();

		RubyIO input = new RubyIO(ruby, request.getInputStream());
		RubyIO errors = new RubyIO(ruby, System.out);

		ruby.evalScriptlet("require %q(org/torquebox/rack/core/environment_builder)");

		RubyModule envBuilder = ruby.getClassFromPath("TorqueBox::Rack::EnvironmentBuilder");

		return JavaEmbedUtils.invokeMethod(ruby, envBuilder, "build", new Object[] { context, request, input, errors }, Object.class);
	}

	public RackResponse call(Object env) {
		IRubyObject response = (RubyArray) JavaEmbedUtils.invokeMethod(this.rubyApp.getRuntime(), this.rubyApp, "call", new Object[] { env }, RubyArray.class);
		return new RackResponseImpl(response);
	}
}
