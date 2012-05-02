/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

package org.torquebox.web.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jruby.Ruby;
import org.jruby.exceptions.RaiseException;
import org.projectodd.polyglot.web.servlet.HttpServletResponseCapture;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.web.component.RackApplicationComponent;
import org.torquebox.web.rack.RackEnvironment;

public class RackFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {

        ServiceRegistry registry = (ServiceRegistry) filterConfig.getServletContext().getAttribute( "service.registry" );

        ServiceName componentResolverServiceName = (ServiceName) filterConfig.getServletContext().getAttribute( "component.resolver.service-name" );
        this.componentResolver = (ComponentResolver) registry.getService( componentResolverServiceName ).getValue();
        if (this.componentResolver == null) {
            throw new ServletException( "Unable to obtain Rack component resolver: " + componentResolverServiceName );
        }

        ServiceName runtimePoolServiceName = (ServiceName) filterConfig.getServletContext().getAttribute( "runtime.pool.service-name" );
        this.runtimePool = (RubyRuntimePool) registry.getService( runtimePoolServiceName ).getValue();

        if (this.runtimePool == null) {
            throw new ServletException( "Unable to obtain runtime pool: " + runtimePoolServiceName );
        }

        this.servletContext = filterConfig.getServletContext();
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            doFilter( (HttpServletRequest) request, (HttpServletResponse) response, chain );
        }
    }

    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (((request.getPathInfo() == null) || (request.getPathInfo().equals( "/" ))) && !(request.getRequestURI().endsWith( "/" ))) {
            String redirectUri = request.getRequestURI() + "/";
            String queryString = request.getQueryString();
            if (queryString != null) {
                redirectUri = redirectUri + "?" + queryString;
            }
            redirectUri = response.encodeRedirectURL( redirectUri );
            response.sendRedirect( redirectUri );
            return;
        }

        HttpServletResponseCapture responseCapture = new HttpServletResponseCapture( response );
        try {
            chain.doFilter( request, responseCapture );
            if (responseCapture.isError()) {
                response.reset();
            } else if (!request.getMethod().equals( "OPTIONS" )) {
                // Pass HTTP OPTIONS requests through to the Rack application
                return;
            }
        } catch (ServletException e) {
            log.error( "Error performing request", e );
        }
        doRack( request, response );
    }

    protected void doRack(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        RackEnvironment rackEnv = null;

        Ruby runtime = null;
        RackApplicationComponent rackApp;
        try {
            runtime = this.runtimePool.borrowRuntime( "rack" );
            rackApp = (RackApplicationComponent) this.componentResolver.resolve( runtime );
            rackEnv = new RackEnvironment( runtime, servletContext, request );
            rackApp.call( rackEnv ).respond( response );
        } catch (RaiseException e) {
            log.error( "Error invoking Rack filter", e );
            log.error( "Underlying Ruby exception", e.getCause() );
            throw new ServletException( e );
        } catch (Exception e) {
            log.error( "Error invoking Rack filter", e );
            throw new ServletException( e );
        } finally {
            if (rackEnv != null) {
                rackEnv.close();
            }

            if (runtime != null) {
                this.runtimePool.returnRuntime( runtime );
            }
        }
    }

    private static final Logger log = Logger.getLogger( RackFilter.class );

    public static final String RACK_APP_DEPLOYMENT_INIT_PARAM = "torquebox.rack.app.deployment.name";

    private ComponentResolver componentResolver;
    private RubyRuntimePool runtimePool;
    private ServletContext servletContext;
}
