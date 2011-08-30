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

package org.torquebox.core.component;

import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.core.util.ReflectionHelper;

public class AbstractRubyComponent implements RubyComponent {

    public AbstractRubyComponent() {
    }

    public AbstractRubyComponent(IRubyObject rubyComponent) {
        this.rubyComponent = rubyComponent;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public Map<String, Object> getOptions() {
        return this.options;
    }

    public Object getOption(String name) {
        return this.options.get( name );
    }

    public void setRubyComponent(IRubyObject rubyComponent) {
        this.rubyComponent = rubyComponent;
    }

    public IRubyObject getRubyComponent() {
        return this.rubyComponent;
    }

    protected Object _callRubyMethod(Object target, String method, Object... args) {
        return JavaEmbedUtils.invokeMethod( this.rubyComponent.getRuntime(), target, method, args, Object.class );
    }

    public Object _callRubyMethod(String method, Object... args) {
        return _callRubyMethod( this.rubyComponent, method, args );
    }

    protected Object _callRubyMethodIfDefined(Object target, String method, Object... args) {
        return ReflectionHelper.callIfPossible( this.rubyComponent.getRuntime(), target, method, args );
    }

    public Object _callRubyMethodIfDefined(String method, Object... args) {
        return _callRubyMethodIfDefined( this.rubyComponent, method, args );
    }

    protected RubyModule getClass(String path) {
        return this.rubyComponent.getRuntime().getClassFromPath( path );
    }

    protected Ruby getRuby() {
        return this.rubyComponent.getRuntime();
    }

    private Map<String, Object> options;
    private IRubyObject rubyComponent;
}
