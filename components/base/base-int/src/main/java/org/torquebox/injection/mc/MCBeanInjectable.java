package org.torquebox.injection.mc;

import org.torquebox.core.injection.SimpleNamedInjectable;

public class MCBeanInjectable extends SimpleNamedInjectable {
    
    public MCBeanInjectable(String name, boolean generic) {
        super( "mc", name, generic );
    }

}
