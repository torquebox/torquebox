package org.torquebox.injection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.torquebox.injection.spi.InjectableRegistry;

public class InjectableRegistryImpl implements InjectableRegistry {
    
    private Map<String,InjectableCollection> collections = new HashMap<String,InjectableCollection>();
    private Map<String, Injectable> genericInjections;
    
    public InjectableRegistryImpl() {
        
    }
    
    public void create() {
        System.err.println( "CREATE InjectableRegistryImpl: " + this.collections );
    }
    
    public void start() {
        System.err.println( "START InjectableRegistryImpl: " + this.collections );
    }
    
    public void setCollections(List<InjectableCollection> collections) {
        System.err.println( "SET COLLECTIONS: " + collections );
        this.collections.clear();
        
        for ( InjectableCollection each : collections ) {
            this.collections.put( each.getName(), each );
        }
    }
    
    public List<InjectableCollection> getCollections() {
        ArrayList<InjectableCollection> list = new ArrayList<InjectableCollection>();
        list.addAll( this.collections.values() );
        return list;
    }
    
    public void setGenericInjections(Map<String, Injectable> genericInjections) {
        this.genericInjections = genericInjections;
    }
    
    public Map<String, Injectable> getGenericInjections() {
        return this.genericInjections;
    }
    
    public Set<String> getCollectionNames() {
        return this.collections.keySet();
    }
    
    public InjectableCollection getCollection(String name) {
        return this.collections.get( name );
    }
    
    public Object get(String collectionName, String objectName) {
        if ( collectionName == null ) {
            return this.genericInjections.get(  objectName  );
        }
        
        InjectableCollection collection = getCollection(collectionName);
        
        if ( collection == null ) {
            return null;
        }
        
        return collection.get( objectName );
    }

    public String toString() {
        StringBuilder result = new StringBuilder("[InjectableRegistryImpl:\n");
        for (InjectableCollection collection: this.collections.values()) {
            result.append( "  " + collection.toString() + "\n" );
        }
        result.append("]");
        return result.toString();
    }

}
