package org.torquebox.core.injection.jndi;

import org.torquebox.core.injection.SimpleNamedInjectable;


public class JNDIInjectable extends SimpleNamedInjectable {
    
    public JNDIInjectable(String name, boolean generic) {
        this( "jndi", name, generic );
    }
    
    protected JNDIInjectable(String type, String name, boolean generic) {
        super( type, name, generic );
    }

}
