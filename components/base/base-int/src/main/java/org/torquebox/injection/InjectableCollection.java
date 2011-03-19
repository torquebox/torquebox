package org.torquebox.injection;

import java.util.Map;

import org.jboss.beans.metadata.api.annotations.Create;
import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.plugins.AbstractDependencyValueMetaData;

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
    
    @Create
    public void create() {
        System.err.println( "CREATE InjectableCollection: " + this );
    }
    
    @Start
    public void start() {
        System.err.println( "START InjectableCollection: " + this );
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
