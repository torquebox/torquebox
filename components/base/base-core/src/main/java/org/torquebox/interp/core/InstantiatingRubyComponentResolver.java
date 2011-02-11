/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

package org.torquebox.interp.core;

import java.util.Collection;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.interp.spi.ComponentInitializer;

public class InstantiatingRubyComponentResolver extends ManagedComponentResolver {

    private String rubyClassName;
    private String rubyRequirePath;
    private Object[] initializeParams;

    private static final Logger log = Logger.getLogger( InstantiatingRubyComponentResolver.class );

    private ComponentInitializer componentInitializer;

    public InstantiatingRubyComponentResolver() {
    }

    public void setRubyClassName(String rubyClassName) {
        this.rubyClassName = rubyClassName;
    }

    public String getRubyClassName() {
        return this.rubyClassName;
    }

    public void setRubyRequirePath(String rubyRequirePath) {
        this.rubyRequirePath = rubyRequirePath;
    }

    public String getRubyRequirePath() {
        return this.rubyRequirePath;
    }

    public void setInitializeParams(Object[] initializeParams) {
        this.initializeParams = initializeParams;
    }

    public void setInitializeParams(Collection params) {
        if (params != null)
            setInitializeParams( params.toArray() );
    }

    public void setInitializeParams(Map params) {
        if (params != null)
            setInitializeParams( new Object[] { params } );
    }

    public Object[] getInitializeParams() {
        return this.initializeParams;
    }

    public void setComponentInitializer(ComponentInitializer componentInitializer) {
        this.componentInitializer = componentInitializer;
    }

    public ComponentInitializer getComponentInitializer() {
        return this.componentInitializer;
    }

    protected IRubyObject createComponent(Ruby ruby) throws Exception {
        log.debug( "createComponent(" + ruby + ")" );
        if (this.rubyRequirePath != null) {
            ruby.getLoadService().load( this.rubyRequirePath + ".rb", false );
            log.debug( "Loaded source file: " + this.rubyRequirePath + ".rb" );
        }

        RubyClass componentClass = (RubyClass) ruby.getClassFromPath( this.rubyClassName );
        log.debug( "Got componentClass: " + componentClass );
        if (componentClass == null || componentClass.isNil()) {
            return null;
        }

        IRubyObject component = (IRubyObject) JavaEmbedUtils.invokeMethod( ruby, componentClass, "new", getInitializeParams(), IRubyObject.class );
        log.debug( "Got component: " + component );
        if (this.componentInitializer != null) {
            this.componentInitializer.initialize( component );
            log.debug( "Initialized component" );
        }

        return component;
    }

}
