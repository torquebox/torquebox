package org.torquebox.injection;

import org.jruby.ast.Node;

public class LoggerInjectableHandler extends AbstractInjectableHandler {
    
    public LoggerInjectableHandler() {
        super( LoggerInjectable.TYPE );
    }

    public Injectable handle(Node node) {
        String name = getString( node );
        return new LoggerInjectable( name );
    }
    
}