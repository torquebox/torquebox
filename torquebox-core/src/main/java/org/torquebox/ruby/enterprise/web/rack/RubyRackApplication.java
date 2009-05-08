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
package org.torquebox.ruby.enterprise.web.rack;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyIO;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.ruby.core.util.StringUtils;
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
		String fullScript = "require %q(rack/builder)\n" + script;
		rubyApp = this.ruby.evalScriptlet(fullScript);		
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

	public RackResponse call(Object env) {
		IRubyObject response = (RubyArray) JavaEmbedUtils.invokeMethod(this.ruby, this.rubyApp, "call", new Object[] { env },
				RubyArray.class);
		return new RubyRackResponse(response);
	}
	
	public void dispatchSipRequest(SipServletRequest request, String rubyClassName) {
		try {
			loadSupport(ruby);
			
			String requirePath = StringUtils.underscore(rubyClassName).replaceAll(
					"::", "/");
			String require = "load %q(" + requirePath + ".rb)";
	
			ruby.evalScriptlet(require);
	
			RubyModule rubyClass = ruby.getClassFromPath(rubyClassName);
	
			SipServlet sipHandler = (SipServlet)JavaEmbedUtils.invokeMethod(ruby, rubyClass, "new",
					EMPTY_OBJECT_ARRAY, SipServlet.class);
	
			IRubyObject rubySipHandler = JavaEmbedUtils
					.javaToRuby(ruby, sipHandler);
	
			JavaEmbedUtils.invokeMethod(ruby, rubySipHandler,
					"service", new Object[] { request, null }, void.class);
		} catch (ClassCastException e) {
			log.error(rubyClassName + " is not a SipServlet subclass", e);
			throw e;
		}
	}
	
	public void dispatchSipResponse(SipServletResponse response, String rubyClassName) {
		try {
			loadSupport(ruby);
			
			String requirePath = StringUtils.underscore(rubyClassName).replaceAll(
					"::", "/");
			String require = "load %q(" + requirePath + ".rb)";
	
			ruby.evalScriptlet(require);
	
			RubyModule rubyClass = ruby.getClassFromPath(rubyClassName);
	
			SipServlet sipHandler = (SipServlet)JavaEmbedUtils.invokeMethod(ruby, rubyClass, "new",
					EMPTY_OBJECT_ARRAY, SipServlet.class);
	
			IRubyObject rubySipHandler = JavaEmbedUtils
					.javaToRuby(ruby, sipHandler);
	
			JavaEmbedUtils.invokeMethod(ruby, rubySipHandler,
					"service", new Object[] { null, response }, void.class);		
		} catch (ClassCastException e) {
			log.error(rubyClassName + " is not a SipServlet subclass", e);
			throw e;
		}
	}

	protected void loadSupport(Ruby runtime) {
		String supportScript = "require %q(org/torquebox/ruby/enterprise/sip/sip_base_handler)\n";
		runtime.evalScriptlet(supportScript);
	}
}
