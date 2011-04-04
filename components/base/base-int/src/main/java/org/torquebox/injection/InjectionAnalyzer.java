package org.torquebox.injection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.jboss.vfs.VirtualFile;
import org.torquebox.interp.analysis.ScriptAnalyzer;
import org.torquebox.interp.metadata.RubyRuntimeMetaData.Version;

public class InjectionAnalyzer extends ScriptAnalyzer {

    public InjectionAnalyzer() {
    }

    public void setInjectableHandlerRegistry(InjectableHandlerRegistry registry) {
        this.injectableHandlerRegistry = registry;
    }

    public InjectableHandlerRegistry getInjectableHandlerRegistry() {
        return this.injectableHandlerRegistry;
    }

    public List<Injectable> analyze(String filename, InputStream script, Version rubyVersion) throws IOException {
        InjectionVisitor visitor = new InjectionVisitor( this );
        analyze( filename, script, visitor, rubyVersion );
        return visitor.getInjectables();
    }

    public List<Injectable> analyze(String filename, String script, Version rubyVersion) {
        InjectionVisitor visitor = new InjectionVisitor( this );
        analyze( filename, script, visitor, rubyVersion );
        return visitor.getInjectables();
    }

    public List<Injectable> analyze(VirtualFile file, Version rubyVersion) throws IOException {
        if ( ! file.exists() ) {
            return Collections.emptyList();
        }
        InjectionVisitor visitor = new InjectionVisitor( this );
        InputStream in = null;
        
        try {
            in = file.openStream();
            analyze( file.getPathName(), in, visitor, rubyVersion );
        } finally {
            if ( in != null ) {
                in.close();
            }
        }
        return visitor.getInjectables();
    }
    
    public List<Injectable> analyzeRecursively(VirtualFile root, Version rubyVersion) throws IOException {
        InjectionAnalyzerVirtualFileVisitor fileVisitor = new InjectionAnalyzerVirtualFileVisitor( this, rubyVersion );
        root.visit( fileVisitor );
        return fileVisitor.getInjectables();
    }

    private InjectableHandlerRegistry injectableHandlerRegistry;
}
