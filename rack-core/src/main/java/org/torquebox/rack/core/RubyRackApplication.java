/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
import org.torquebox.ruby.enterprise.web.rack.spi.RackApplication;
import org.torquebox.ruby.enterprise.web.rack.spi.RackResponse;

public class RubyRackApplication implements RackApplication {
	private static final Logger log = Logger.getLogger(RubyRackApplication.class);
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};
	private Ruby ruby;
	private IRubyObject rubyApp;

	public RubyRackApplication(Ruby ruby, String rackUpScript) {
		this.ruby = ruby;
		rackUp(rackUpScript);
	}

	private void rackUp(String script) {
		String fullScript = "require %q(rack/builder)\n" + 
			"require %q(org/torquebox/ruby/enterprise/web/rack/middleware/reloader)\n" +
			"Rack::Builder.new{(\n" + 
				script + 
			"\n)}.to_app";

		rubyApp = this.ruby.executeScript(fullScript, "RubyRackApplication/rackup.rb");
	}

	public Object createEnvironment(ServletContext context, HttpServletRequest request) throws Exception {
		Ruby ruby = rubyApp.getRuntime();

		RubyIO input = new RubyIO(ruby, request.getInputStream());
		RubyIO errors = new RubyIO(ruby, System.out);

		ruby.evalScriptlet("require %q(org/torquebox/ruby/enterprise/web/rack/environment_builder)");

		RubyModule envBuilder = ruby.getClassFromPath("JBoss::Rack::EnvironmentBuilder");

		return JavaEmbedUtils.invokeMethod(ruby, envBuilder, "build", new Object[] { context, request, input, errors },
				Object.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object createEnvironment(ServletContext context, SipServletMessage message, String sipRubyControllerName)
			throws Exception {
		Ruby ruby = rubyApp.getRuntime();

		RubyIO errors = new RubyIO(ruby, System.out);

		ruby.evalScriptlet("require %q(org/torquebox/ruby/enterprise/sip/sip_environment_builder)");

		RubyModule envBuilder = ruby.getClassFromPath("JBoss::Rack::SipEnvironmentBuilder");

		if (message instanceof SipServletRequest) {
			return JavaEmbedUtils.invokeMethod(ruby, envBuilder, "build_env_request", new Object[] { context, message,
					sipRubyControllerName, errors }, Object.class);
		} else {
			return JavaEmbedUtils.invokeMethod(ruby, envBuilder, "build_env_response", new Object[] { context, message,
					sipRubyControllerName, errors }, Object.class);
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
		IRubyObject response = (RubyArray) JavaEmbedUtils.invokeMethod(this.ruby, this.rubyApp, "call",
				new Object[] { env }, RubyArray.class);
		return new RubyRackResponse(response);
	}

	/**
	 * 
	 * @param request
	 * @param rubyClassName
	 */
	protected void dispatchSipMessage(SipServletMessage message, String sipRubyControllerName) {
		try {
			String requirePath = StringUtils.underscore(sipRubyControllerName).replaceAll("::", "/");
			String require = "load %q(" + requirePath + ".rb)";

			ruby.evalScriptlet(require);

			RubyModule rubyClass = ruby.getClassFromPath(sipRubyControllerName);

			SipServlet sipHandler = (SipServlet) JavaEmbedUtils.invokeMethod(ruby, rubyClass, "new",
					EMPTY_OBJECT_ARRAY, SipServlet.class);

			IRubyObject rubySipHandler = JavaEmbedUtils.javaToRuby(ruby, sipHandler);

			if (message instanceof SipServletRequest) {
				JavaEmbedUtils
						.invokeMethod(ruby, rubySipHandler, "service", new Object[] { message, null }, void.class);
			} else {
				JavaEmbedUtils
						.invokeMethod(ruby, rubySipHandler, "service", new Object[] { null, message }, void.class);
			}
		} catch (ClassCastException e) {
			log.error(sipRubyControllerName + " is not a SipServlet subclass", e);
			throw e;
		}
	}
}
