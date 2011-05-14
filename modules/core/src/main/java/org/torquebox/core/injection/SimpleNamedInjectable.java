package org.torquebox.core.injection;

import org.torquebox.core.injection.analysis.Injectable;

public abstract class SimpleNamedInjectable implements Injectable {
    
    private String type;
    private String name;
    private boolean generic;

    public SimpleNamedInjectable(String type, String name, boolean generic) {
        this.type = type;
        this.name = name;
        this.generic = generic;
    }
    
    public boolean isGeneric() {
        return this.generic;
    }
    
    public String getType() {
        return this.type;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getKey() {
        return getName();
    }
    
    public String toString() {
        return "[" + getClass().getName() + ": " + this.name + "]";
    }


}
