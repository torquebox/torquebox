package org.torquebox.core.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.as.naming.ValueManagedObject;
import org.jboss.msc.inject.InjectionException;
import org.jboss.msc.inject.Injector;

public class Injections {
    
    public Injections() {
        
    }
    
    public Injector<Object> getInjector(final String key) {
        return new Injector<Object>() {
            @Override
            public void inject(Object value) throws InjectionException {
                System.err.println( "COMPONENT INJECTION: " + value );
                System.err.println( "COMPONENT INJECTION ref: " + ((ValueManagedObject)value).getReference() );
                System.err.println( "COMPONENT INJECTION ref.instance: " + ((ValueManagedObject)value).getReference().getInstance() );
                Injections.this.injections.put( key, value );
            }

            @Override
            public void uninject() {
                Injections.this.injections.remove( key );
            }
            
        };
    }
    
    private Map<String,Object> injections = new ConcurrentHashMap<String,Object>();
}
