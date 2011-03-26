package org.torquebox.injection;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
    
    
    @SuppressWarnings("unchecked")
    public List<Injectable> analyze(String filename, InputStream script) throws IOException {
        InjectionVisitor visitor = new InjectionVisitor( this );
        analyze( filename, script, visitor );
        return visitor.getInjectables();
    }
    
    @SuppressWarnings("unchecked")
    public List<Injectable> analyze(String filename, String script) {
        InjectionVisitor visitor = new InjectionVisitor( this );
        analyze( filename, script, visitor );
        return visitor.getInjectables();
    }
    
    private InjectableHandlerRegistry injectableHandlerRegistry;
}
