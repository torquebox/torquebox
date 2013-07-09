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

package org.torquebox.core.component;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.core.util.RuntimeHelper;

public class ComponentClass implements ComponentInstantiator {

    private String className;
    private String requirePath;

    public ComponentClass() {

    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    public void setRequirePath(String requirePath) {
        this.requirePath = requirePath;
    }

    public String getRequirePath() {
        return this.requirePath;
    }

    public IRubyObject newInstance(Ruby runtime, Object[] initParams, boolean alwaysReload) {
        if (alwaysReload) {
            // if always reloading we synchronize the loading and instantiation so someone
            // doesn't reload things while we're in the middle of instantiating from another thread
            synchronized(("ComponentClassLock" + this.className).intern()) {
                if (this.requirePath != null) {
                    runtime.getLoadService().load(this.requirePath + ".rb", false);
                }

                return RuntimeHelper.instantiate(runtime, this.className, initParams);
            }
        } else {
            // if not always reloading we only synchronize the loading and use
            // require instead of load so it only happens once
            synchronized(("ComponentClassLock" + this.className).intern()) {
                if (this.requirePath != null) {
                    runtime.getLoadService().require(this.requirePath);
                }
            }
            return RuntimeHelper.instantiate(runtime, this.className, initParams);
        }
    }
    
    public String toString() {
        return this.className;
    }

}
