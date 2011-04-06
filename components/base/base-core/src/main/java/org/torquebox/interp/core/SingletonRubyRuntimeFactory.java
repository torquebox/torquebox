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

import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig.CompileMode;

import org.torquebox.interp.spi.RubyRuntimeFactory;

public class SingletonRubyRuntimeFactory implements RubyRuntimeFactory {

    private Ruby ruby;

    public SingletonRubyRuntimeFactory() {

    }

    public void setRuby(Ruby ruby) {
        this.ruby = ruby;
    }

    public Ruby getRuby() {
        return this.ruby;
    }

    @Override
    public Ruby createInstance(String contextInfo) throws Exception {
        return getRuby();
    }

    @Override
    public void destroyInstance(Ruby instance) {
        // no-op, we didn't create the ruby.
    }

    public CompatVersion getRubyVersion() {
        if (this.ruby.is1_9()) {
            return CompatVersion.RUBY1_9;
        }

        return CompatVersion.RUBY1_8;
    }

    public CompileMode getCompileMode() {
        return this.ruby.getInstanceConfig().getCompileMode();
    }
}
