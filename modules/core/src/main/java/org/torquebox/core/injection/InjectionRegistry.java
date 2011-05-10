package org.torquebox.core.injection;

import java.util.HashMap;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.torquebox.core.injection.ConvertableRubyInjection;

public class InjectionRegistry {

    public InjectionRegistry() {
    }
    
    public void setInjectionRegistry(Map<String, Object> registry) {
        this.registry = registry;
    }

    public void merge(Ruby ruby) {
        RubyModule torqueboxRegistry = ruby.getClassFromPath( TORQUEBOX_REGISTRY_CLASS_NAME );
        JavaEmbedUtils.invokeMethod( ruby, torqueboxRegistry, "merge!", new Object[] { getConvertedRegistry( ruby ) }, void.class );
    }

    protected Map<String, Object> getConvertedRegistry(Ruby ruby) {
        Map<String, Object> convertedRegistry = new HashMap<String, Object>();

        for (String key : this.registry.keySet()) {
            convertedRegistry.put( key, convert( ruby, this.registry.get( key ) ) );
        }

        return convertedRegistry;
    }
    
    public Object getUnconverted(String key) {
        return this.registry.get( key );
    }

    protected Object convert(Ruby ruby, Object object) {
        if (object instanceof ConvertableRubyInjection) {
            return ((ConvertableRubyInjection) object).convert( ruby );
        }
        return object;
    }

    public static final String TORQUEBOX_REGISTRY_CLASS_NAME = "TorqueBox::Registry";
    private Map<String, Object> registry;

}
