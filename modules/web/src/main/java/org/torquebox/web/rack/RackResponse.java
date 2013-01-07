/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.web.rack;

import javax.servlet.http.HttpServletResponse;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.core.util.RuntimeHelper;

public class RackResponse {
    public static final String RESPONSE_HANDLER_RB = "org/torquebox/web/rack/response_handler";
    public static final String RESPONSE_HANDLER_CLASS_NAME = "TorqueBox::Rack::ResponseHandler";
    public static final String RESPONSE_HANDLER_METHOD_NAME = "handle";

    private IRubyObject rackResponse;

    public RackResponse(IRubyObject rackResponse) {
        this.rackResponse = rackResponse;
    }

    public void respond(HttpServletResponse response) {
        Ruby ruby = rackResponse.getRuntime();
        RuntimeHelper.requireUnlessDefined( ruby, RESPONSE_HANDLER_RB, RESPONSE_HANDLER_CLASS_NAME );
        RuntimeHelper.invokeClassMethod( ruby, RESPONSE_HANDLER_CLASS_NAME, RESPONSE_HANDLER_METHOD_NAME, new Object[] { rackResponse, response}); 
    }

}
