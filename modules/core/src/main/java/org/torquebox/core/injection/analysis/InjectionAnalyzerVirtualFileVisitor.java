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
        
    };

}
