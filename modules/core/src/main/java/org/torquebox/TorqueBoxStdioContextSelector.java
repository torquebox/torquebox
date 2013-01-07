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

package org.torquebox;

import org.jboss.stdio.StdioContext;
import org.jboss.stdio.StdioContextSelector;
import org.jruby.Ruby;
import org.torquebox.core.runtime.RuntimeContext;
import org.torquebox.core.runtime.TorqueBoxRubyInstanceConfig;

public class TorqueBoxStdioContextSelector implements StdioContextSelector {

    /**
     * Construct a new instance.
     *
     * @param defaultContext the context to use (must not be {@code null})
     * @param debugContext the context to use for the Ruby debugger (must not be {@code null})
     */
    public TorqueBoxStdioContextSelector(final StdioContext defaultContext, final StdioContext debugContext) {
        if (defaultContext == null) {
            throw new NullPointerException( "defaultContext is null" );
        }
        if (debugContext == null) {
            throw new NullPointerException( "debugContext is null" );
        }
        this.defaultContext = defaultContext;
        this.debugContext = debugContext;
    }

    /** {@inheritDoc} */
    public StdioContext getStdioContext() {
        Ruby runtime = RuntimeContext.getCurrentRuntime();
        if (runtime != null && runtime.getInstanceConfig() instanceof TorqueBoxRubyInstanceConfig) {
            TorqueBoxRubyInstanceConfig config = (TorqueBoxRubyInstanceConfig) runtime.getInstanceConfig();
            if (config.isInteractive()) {
                return debugContext;
            }
        }
        return defaultContext;
    }


    private final StdioContext defaultContext;
    private final StdioContext debugContext;

}
