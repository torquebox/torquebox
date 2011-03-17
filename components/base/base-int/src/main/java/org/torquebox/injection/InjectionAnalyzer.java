package org.torquebox.injection;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.torquebox.interp.analysis.ScriptAnalyzer;

public class InjectionAnalyzer extends ScriptAnalyzer {

    public InjectionAnalyzer() {
        setNodeVisitor( new InjectionVisitor( this ) );
    }
    
    public void setInjectableHandlerRegistry(InjectableHandlerRegistry registry) {
        this.injectableHandlerRegistry = registry;
    }
    
    public InjectableHandlerRegistry getInjectableHandlerRegistry() {
        return this.injectableHandlerRegistry;
    }
    
    
    @SuppressWarnings("unchecked")
    public List<Injectable> analyze(InputStream script) throws IOException {
        Object result = super.analyze( script );
        if ( result instanceof List ) {
            return (List<Injectable>) result;
        }
        
        List<Injectable> injections = new ArrayList<Injectable>();
        injections.add( (Injectable) result );
        return injections;
    }
    
    @SuppressWarnings("unchecked")
    public List<Injectable> analyze(String script) {
        Object result = super.analyze( script );
        if ( result instanceof List ) {
            return (List<Injectable>) result;
        }
        
        List<Injectable> injections = new ArrayList<Injectable>();
        injections.add( (Injectable) result );
        return injections;
    }
    
    private InjectableHandlerRegistry injectableHandlerRegistry;
}
