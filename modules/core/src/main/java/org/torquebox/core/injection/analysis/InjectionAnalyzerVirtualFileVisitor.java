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

package org.torquebox.core.injection.analysis;

import java.io.IOException;

import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileVisitor;
import org.jboss.vfs.VisitorAttributes;
import org.torquebox.core.runtime.RubyRuntimeMetaData.Version;

public class InjectionAnalyzerVirtualFileVisitor implements VirtualFileVisitor {
    
    
    public InjectionAnalyzerVirtualFileVisitor(InjectionIndex index, InjectionAnalyzer analyzer, Version rubyVersion) {
        this.index       = index;
        this.analyzer    = analyzer;
        this.rubyVersion = rubyVersion;
        this.byteCodeVisitor = new InjectionRubyByteCodeVisitor( this.analyzer );
    }

    @Override
    public VisitorAttributes getAttributes() {
        return InjectionAnalyzerVirtualFileVisitor.ATTRIBUTES;
    }

    @Override
    public void visit(VirtualFile file) {
        if ( shouldVisit(file) ) {
            try {
                this.byteCodeVisitor.reset();
                this.analyzer.analyze( file, this.byteCodeVisitor, this.rubyVersion );
                this.index.addInjectables( file, this.byteCodeVisitor.getInjectables() );
            } catch (IOException e) {
                throw new InjectionException( e );
            } finally {
                this.byteCodeVisitor.reset();
            }
        }
    }
    
    protected boolean shouldVisit(VirtualFile file) {
        String name = file.getName();
        return ( name.endsWith( ".rb"  ) || name.endsWith( ".ru"  ) );
    }
    
    private InjectionIndex index;
    private InjectionAnalyzer analyzer;
    private Version rubyVersion;
    private InjectionRubyByteCodeVisitor byteCodeVisitor;
    
    private static final VisitorAttributes ATTRIBUTES = new VisitorAttributes() {

        @Override
        public boolean isLeavesOnly() {
            return true;
        }

        @Override
        public boolean isIncludeRoot() {
            return true;
        }
        
        @Override
        public boolean isRecurse(VirtualFile file) {
            return true;
        }
        
    };

}
