/* Copyright 2009 Red Hat, Inc. */
package org.torquebox.rack.core;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;

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
public class RubyRackApplication implements RackApplication {

	/** Log. */
	private static final Logger log = Logger.getLogger(RubyRackApplication.class);

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
	public RubyRackApplication(Ruby ruby, String rackUpScript) {
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

		ruby.evalScriptlet("require %q(org/torquebox/ruby/enterprise/web/rack/environment_builder)");

		RubyModule envBuilder = ruby.getClassFromPath("JBoss::Rack::EnvironmentBuilder");

		return JavaEmbedUtils.invokeMethod(ruby, envBuilder, "build", new Object[] { context, request, input, errors }, Object.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object createEnvironment(ServletContext context, SipServletMessage message, String sipRubyControllerName) throws Exception {
		Ruby ruby = rubyApp.getRuntime();

		RubyIO errors = new RubyIO(ruby, System.out);

		ruby.evalScriptlet("require %q(org/torquebox/ruby/enterprise/sip/sip_environment_builder)");

		RubyModule envBuilder = ruby.getClassFromPath("JBoss::Rack::SipEnvironmentBuilder");

		if (message instanceof SipServletRequest) {
			return JavaEmbedUtils.invokeMethod(ruby, envBuilder, "build_env_request", new Object[] { context, message, sipRubyControllerName, errors }, Object.class);
		} else {
			return JavaEmbedUtils.invokeMethod(ruby, envBuilder, "build_env_response", new Object[] { context, message, sipRubyControllerName, errors }, Object.class);
		}
	}

	public RackResponse call(Object env) {
		// SIP specifics
		if (env instanceof RubyHash) {
			RubyHash rackEnv = (RubyHash) env;
			String sipRubyControllerName = (String) rackEnv.get("sip_ruby_controller_name");
			SipServletMessage sipServletMessage = (SipServletMessage) rackEnv.get("sip_servlet_message");
			if (sipRubyControllerName != null) {
				dispatchSipMessage(sipServletMessage, sipRubyControllerName);
				return null;
			}
		}
		IRubyObject response = (RubyArray) JavaEmbedUtils.invokeMethod(this.rubyApp.getRuntime(), this.rubyApp, "call", new Object[] { env }, RubyArray.class);
		return new RubyRackResponse(response);
	}

	protected void dispatchSipMessage(SipServletMessage message, String sipRubyControllerName) {
		try {
			String requirePath = StringUtils.underscore(sipRubyControllerName).replaceAll("::", "/");
			String require = "load %q(" + requirePath + ".rb)";

			this.rubyApp.getRuntime().evalScriptlet(require);

			RubyModule rubyClass = this.rubyApp.getRuntime().getClassFromPath(sipRubyControllerName);

			SipServlet sipHandler = (SipServlet) JavaEmbedUtils.invokeMethod(this.rubyApp.getRuntime(), rubyClass, "new", EMPTY_OBJECT_ARRAY, SipServlet.class);

			IRubyObject rubySipHandler = JavaEmbedUtils.javaToRuby(this.rubyApp.getRuntime(), sipHandler);

			if (message instanceof SipServletRequest) {
				JavaEmbedUtils.invokeMethod(this.rubyApp.getRuntime(), rubySipHandler, "service", new Object[] { message, null }, void.class);
			} else {
				JavaEmbedUtils.invokeMethod(this.rubyApp.getRuntime(), rubySipHandler, "service", new Object[] { null, message }, void.class);
			}
		} catch (ClassCastException e) {
			log.error(sipRubyControllerName + " is not a SipServlet subclass", e);
			throw e;
		}
	}
}
