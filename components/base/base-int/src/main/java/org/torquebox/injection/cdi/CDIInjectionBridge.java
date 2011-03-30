package org.torquebox.injection.cdi;

import javax.naming.NamingException;

public class CDIInjectionBridge {

    public CDIInjectionBridge(CDIBridge bridge, String typeName) {
        this.bridge = bridge;
        this.typeName = typeName;
    }
    
    public Object getValue() throws ClassNotFoundException, NamingException {
        return bridge.get( this.typeName );
    }
    
    private CDIBridge bridge;
    private String typeName;
}
