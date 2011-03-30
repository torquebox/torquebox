package org.torquebox.injection;

import java.util.Map;

public class InjectableCollection {
    
    private String name;
    private Map<String, Object> map;

    public InjectableCollection() {
        
    }
    public InjectableCollection(String name) {
        System.err.println( "CTOR: " + name );
        this.name = name;
    }
    
    public InjectableCollection(String name, Map<String,Object> map) {
        this.name = name;
        this.map = map;
    }
    
    
    public void setMap(Map<String,Object> map) {
        this.map = map;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Object get(String name) {
        return this.map.get( name );
    }

    public String toString() {
        return "[InjectableCollection: name=" + getName() + ", count=" + this.map.size() + ", map=" + this.map + "]";
    }

}
