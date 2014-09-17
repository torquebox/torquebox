/*
* Copyright 2014 Red Hat, Inc, and individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.projectodd.wunderboss.rack;

import org.jruby.runtime.builtin.IRubyObject;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RackServlet extends GenericServlet {

    public RackServlet(final IRubyObject rackApplication) throws IOException {
        this.rackApplication = new RackApplication(rackApplication);
    }

    @Override
    public final void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            service((HttpServletRequest) request, (HttpServletResponse) response);
        }
    }

    public final void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RackChannel inputChannel = null;
        try {
            inputChannel = rackApplication.getInputChannel(request.getInputStream());
            RackAdapter adapter = new ServletRackAdapter(request, response);
            rackApplication.call(adapter, inputChannel, "java.servlet_request", request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        } finally {
            if (inputChannel != null) {
                inputChannel.close();
            }
        }
    }

    private RackApplication rackApplication;
}
