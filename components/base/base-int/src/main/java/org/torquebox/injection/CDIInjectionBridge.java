package org.torquebox.injection;

import java.lang.reflect.Type;

public class CDIInjectionBridge {


    public CDIInjectionBridge(CDIBridge bridge, String typeName) {
        this.bridge = bridge;
        this.typeName = typeName;
    }
    
    public Object getValue() throws ClassNotFoundException {
        return bridge.get( this.typeName );
    }
    
    private CDIBridge bridge;
    private String typeName;
}
