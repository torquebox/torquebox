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

import javax.servlet.http.HttpServletResponse;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.ruby.enterprise.web.rack.spi.RackResponse;

public class RubyRackResponse implements RackResponse {
	private IRubyObject rackResponse;

	public RubyRackResponse(IRubyObject rackResponse) {
		this.rackResponse = rackResponse;
	}

	public void respond(HttpServletResponse response) {
		Ruby ruby = rackResponse.getRuntime();
		ruby.evalScriptlet( "require %q(org/torquebox/ruby/enterprise/web/rack/response_handler)");
		RubyClass responseHandler = (RubyClass) ruby.getClassFromPath( "JBoss::Rack::ResponseHandler" );
		JavaEmbedUtils.invokeMethod( ruby, responseHandler, "handle", new Object[]{ rackResponse, response }, Object.class );
	}

}
