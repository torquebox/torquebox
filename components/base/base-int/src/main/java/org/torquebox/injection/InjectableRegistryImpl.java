package org.torquebox.injection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.torquebox.injection.spi.InjectableRegistry;

public class InjectableRegistryImpl implements InjectableRegistry {
    
    private Map<String,InjectableCollection> collections = new HashMap<String,InjectableCollection>();
    
    public InjectableRegistryImpl() {
        
    }
    
    public void setCollections(List<InjectableCollection> collections) {
        this.collections.clear();
        
        for ( InjectableCollection each : collections ) {
            this.collections.put( each.getName(), each );
        }
    }
    
    public Set<String> getCollectionNames() {
        return this.collections.keySet();
    }
    
    public InjectableCollection getCollection(String name) {
        return this.collections.get( name );
    }
    
    public Object get(String collectionName, String objectName) {
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
