/* Copyright 2009 Red Hat, Inc. */
package org.torquebox.rack.core;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.rack.spi.RackApplication;
import org.torquebox.rack.spi.RackEnvironment;
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
        ruby.getLoadService().require("rubygems");
		String fullScript = "require %q(vfs)\nrequire %q(rack)\nRack::Builder.new{(\n" + script + "\n)}.to_app";
		IRubyObject app = ruby.executeScript(fullScript, rackUpScriptLocation.toURL().toString());
		return app;
	}

	protected IRubyObject getRubyApplication() {
		return this.rubyApp;
	}

	public Ruby getRuby() {
		return this.rubyApp.getRuntime();
	}

	public RackResponse call(RackEnvironment env) {
		IRubyObject response = (RubyArray) JavaEmbedUtils.invokeMethod(this.rubyApp.getRuntime(), this.rubyApp, "call", new Object[] { env.getEnv() }, RubyArray.class);
		return new RackResponseImpl(response);
	}
	
}
