package org.torquebox.injection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.jboss.vfs.VirtualFile;
import org.torquebox.interp.analysis.ScriptAnalyzer;

public class InjectionAnalyzer extends ScriptAnalyzer {

    public InjectionAnalyzer() {
    }

    public void setInjectableHandlerRegistry(InjectableHandlerRegistry registry) {
        this.injectableHandlerRegistry = registry;
    }

    public InjectableHandlerRegistry getInjectableHandlerRegistry() {
        return this.injectableHandlerRegistry;
    }

    public List<Injectable> analyze(String filename, InputStream script) throws IOException {
        InjectionVisitor visitor = new InjectionVisitor( this );
        analyze( filename, script, visitor );
        return visitor.getInjectables();
    }

    public List<Injectable> analyze(String filename, String script) {
        InjectionVisitor visitor = new InjectionVisitor( this );
        analyze( filename, script, visitor );
        return visitor.getInjectables();
    }

    public List<Injectable> analyze(VirtualFile file) throws IOException {
        if ( ! file.exists() ) {
            return Collections.emptyList();
        }
        InjectionVisitor visitor = new InjectionVisitor( this );
        InputStream in = null;
        
        try {
            in = file.openStream();
            analyze( file.getPathName(), in, visitor );
        } finally {
            if ( in != null ) {
                in.close();
            }
        }
        return visitor.getInjectables();
    }
    
    public List<Injectable> analyzeRecursively(VirtualFile root) throws IOException {
        InjectionAnalyzerVirtualFileVisitor fileVisitor = new InjectionAnalyzerVirtualFileVisitor( this );
        root.visit( fileVisitor );
        return fileVisitor.getInjectables();
    }

    private InjectableHandlerRegistry injectableHandlerRegistry;
}
