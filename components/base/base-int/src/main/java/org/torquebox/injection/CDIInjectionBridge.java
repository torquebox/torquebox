package org.torquebox.injection;

import javax.naming.NamingException;

public class CDIInjectionBridge {


    public CDIInjectionBridge(CDIBridge bridge, String typeName) {
        System.err.println( "CTOR: " + bridge + ", " + typeName );
        this.bridge = bridge;
        this.typeName = typeName;
    }
    
    public void create() {
        System.err.println( "InjectionBridge create() " + this.typeName );
    }
    
    public void start() {
        System.err.println( "InjectionBridge start() " + this.typeName );
    }
    
    public Object getValue() throws ClassNotFoundException, NamingException {
        System.err.println ("CDIInjectionBridger.getValue(): " + this.typeName );
        return bridge.get( this.typeName );
    }
    
    private CDIBridge bridge;
    private String typeName;
}
