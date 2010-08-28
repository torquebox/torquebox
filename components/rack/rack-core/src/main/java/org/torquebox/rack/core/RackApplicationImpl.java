/* Copyright 2009 Red Hat, Inc. */
package org.torquebox.rack.core;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyIO;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
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
	 * @param ruby
	 *            The Ruby interpreter to use for this application.
	 * @param rackUpScript
	 *            The rackup script.
	 */
	public RackApplicationImpl(Ruby ruby, String rackUpScript, VirtualFile rackUpScriptLocation) throws Exception {
		this.rubyApp = rackUp(ruby, rackUpScript, rackUpScriptLocation);
	}

	/**
	 * Perform rackup.
	 * 
	 * @param script
	 *            The rackup script.
	 */
	private IRubyObject rackUp(Ruby ruby, String script, VirtualFile rackUpScriptLocation) throws Exception {
		String fullScript = "require %q(rubygems)\nrequire %q(vfs)\nrequire %q(rack)\nRack::Builder.new{(\n" + script + "\n)}.to_app";

		IRubyObject rackRoot = ruby.evalScriptlet("RACK_ROOT");

		IRubyObject app = ruby.executeScript(fullScript, rackUpScriptLocation.toURL().toString());
		return app;
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
	 * @param context
	 *            The servlet context.
	 * @param request
	 *            The servlet request.
	 * 
	 * @return The Ruby Rack request environment.
	 */
	public Object createEnvironment(ServletContext context, HttpServletRequest request) throws Exception {
		Ruby ruby = rubyApp.getRuntime();

		//RubyIO input = new RubyIO(ruby, new NonClosingInputStream(request.getInputStream()));
		RubyIO input = new RubyIO( ruby, request.getInputStream() );
		RubyIO errors = new RubyIO(ruby, System.err);

		ruby.evalScriptlet("require %q(org/torquebox/rack/core/environment_builder)");

		RubyModule envBuilder = ruby.getClassFromPath("TorqueBox::Rack::EnvironmentBuilder");
		Object environment = JavaEmbedUtils.invokeMethod(ruby, envBuilder, "build", new Object[] { context, request, input, errors }, Object.class);

		return environment;
	}

	public RackResponse call(Object env) {
		IRubyObject response = (RubyArray) JavaEmbedUtils.invokeMethod(this.rubyApp.getRuntime(), this.rubyApp, "call", new Object[] { env }, RubyArray.class);
		return new RackResponseImpl(response);
	}
	
	public RubyIO getInputRubyIO(Object env) {
		RubyHash envHash = (RubyHash) env;
		return (RubyIO) envHash.get( "rack.input" );
	}

	class NonClosingInputStream extends InputStream {
		private InputStream target;

		public NonClosingInputStream(InputStream target) {
			this.target = target;
		}
		public int read() throws IOException {
			return target.read();
		}
		// TODO: Temporary hack
		public void close() throws IOException {
			// Not closing to avoid reading a closed stream
		}
	}
}
