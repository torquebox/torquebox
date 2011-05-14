package org.torquebox.core.component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ValueManagedObject;
import org.jboss.msc.inject.InjectionException;
import org.jboss.msc.inject.Injector;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.torquebox.core.injection.ConvertableRubyInjection;

public class InjectionRegistry {
    
    public InjectionRegistry() {
        
    }
    
    public Injector<Object> getInjector(final String key) {
        return new Injector<Object>() {
            @Override
            public void inject(Object value) throws InjectionException {
                InjectionRegistry.this.injections.put( key, value );
            }

            @Override
            public void uninject() {
                InjectionRegistry.this.injections.remove( key );
            }
            
        };
    }
    
    public void merge(Ruby ruby) {
        RubyModule torqueboxRegistry = ruby.getClassFromPath( TORQUEBOX_REGISTRY_CLASS_NAME );
        JavaEmbedUtils.invokeMethod( ruby, torqueboxRegistry, "merge!", new Object[] { getConvertedRegistry( ruby ) }, void.class );
    }

    protected Map<String, Object> getConvertedRegistry(Ruby ruby) {
        Map<String, Object> convertedRegistry = new HashMap<String, Object>();

        for (String key : this.injections.keySet()) {
            convertedRegistry.put( key, convert( ruby, this.injections.get( key ) ) );
        }

        return convertedRegistry;
    }
    
    public Object getUnconverted(String key) {
        return this.injections.get( key );
    }

    protected Object convert(Ruby ruby, Object object) {
        if (object instanceof ConvertableRubyInjection) {
            return ((ConvertableRubyInjection) object).convert( ruby );
        }
        return object;
    }

    public static final String TORQUEBOX_REGISTRY_CLASS_NAME = "TorqueBox::Registry";
    
    private Map<String,Object> injections = new ConcurrentHashMap<String,Object>();
}
