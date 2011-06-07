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

package org.torquebox.core.injection.analysis;

import java.io.IOException;

import org.jboss.vfs.VirtualFile;
import org.torquebox.core.analysis.ScriptAnalyzer;
import org.torquebox.core.runtime.RubyRuntimeMetaData.Version;

public class InjectionAnalyzer extends ScriptAnalyzer {

    public InjectionAnalyzer(InjectableHandlerRegistry registry) {
        this.injectableHandlerRegistry = registry;
    }

    public InjectableHandlerRegistry getInjectableHandlerRegistry() {
        return this.injectableHandlerRegistry;
    }

    public void  analyzeRecursively(InjectionIndex index, VirtualFile root, Version rubyVersion) throws IOException {
        InjectionAnalyzerVirtualFileVisitor fileVisitor = new InjectionAnalyzerVirtualFileVisitor( index, this, rubyVersion );
        root.visit( fileVisitor );
    }

    private InjectableHandlerRegistry injectableHandlerRegistry;
}
