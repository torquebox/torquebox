package org.torquebox.core.injection.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jruby.ast.Node;

public class PredeterminedInjectableHandler extends AbstractInjectableHandler {

    public PredeterminedInjectableHandler(String subsystemName) {
        super( "predetermined_" + subsystemName );
    }

    protected void addInjectable(String name, Injectable injectable) {
        this.injectables.put( name, injectable );
    }

    @Override
    public Injectable handle(Node node, boolean generic) {
        String key = getString( node );
        return this.injectables.get(  key );
    }

    @Override
    public boolean recognizes(Node argsNode) {
        String key = getString( argsNode );
        return this.injectables.containsKey( key );
    }
    
    public Collection<Injectable> getInjectables() {
        return this.injectables.values();
    }

    private final Map<String, Injectable> injectables = new HashMap<String, Injectable>();
}
