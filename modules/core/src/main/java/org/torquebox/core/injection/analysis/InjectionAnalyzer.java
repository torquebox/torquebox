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
