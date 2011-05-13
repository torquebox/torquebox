package org.torquebox.core.component;

import java.util.HashMap;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class ComponentRegistry {

    public static ComponentRegistry getRegistryFor(Ruby runtime) {
        IRubyObject rubyRegistry = null;
        try {
            rubyRegistry = runtime.getObject().getConstant( TORQUEBOX_COMPONENT_REGISTRY );
        } catch (RaiseException e) {
        }

        ComponentRegistry javaRegistry = null;

        if (rubyRegistry == null || rubyRegistry.isNil()) {
            javaRegistry = new ComponentRegistry( runtime );
            rubyRegistry = JavaEmbedUtils.javaToRuby( runtime, javaRegistry );
            runtime.getObject().setConstant( TORQUEBOX_COMPONENT_REGISTRY, rubyRegistry );
        } else {
            javaRegistry = (ComponentRegistry) JavaEmbedUtils.rubyToJava( rubyRegistry );
        }

        return javaRegistry;
    }

    private ComponentRegistry(Ruby runtime) {
        this.runtime = runtime;
    }

    public IRubyObject lookup(String componentName) {
        return this.registry.get( componentName );
    }

    public void register(String componentName, IRubyObject rubyComponent) {
        if (rubyComponent.getRuntime() != this.runtime) {
            throw new IllegalArgumentException( "Component/runtime mismatch" );
        }

        this.registry.put( componentName, rubyComponent );
    }

    private Ruby runtime;
    private Map<String, IRubyObject> registry = new HashMap<String, IRubyObject>();

    private static final String TORQUEBOX_COMPONENT_REGISTRY = "TORQUEBOX_COMPONENT_REGISTRY";
}
