package org.torquebox.core.component;

import java.util.Map;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public class ComponentResolver {

    public ComponentResolver() {

    }

    public RubyComponent resolve(final Ruby runtime) throws InstantiationException, IllegalAccessException {
        final ComponentRegistry registry = ComponentRegistry.getRegistryFor( runtime );
        IRubyObject rubyComponent = null;

        if (!this.alwaysReload) {
            rubyComponent = registry.lookup( this.componentName );
        }

        if (rubyComponent == null) {
            rubyComponent = createComponent( runtime );
            registry.register( this.componentName, rubyComponent );
        }

        if (rubyComponent == null) {
            return null;
        }

        return wrapComponent( rubyComponent );
    }

    protected IRubyObject createComponent(final Ruby runtime) {
        // TODO injections
        IRubyObject rubyComponent = this.componentInstantiator.newInstance( runtime, this.initializeParams );
        return rubyComponent;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentName() {
        return this.componentName;
    }

    public void setComponentInstantiator(ComponentInstantiator componentInstantiator) {
        this.componentInstantiator = componentInstantiator;
    }

    public ComponentInstantiator getComponentInstantiator() {
        return this.componentInstantiator;
    }

    public void setInitializeParams(Object[] initializeParams) {
        this.initializeParams = initializeParams;
    }

    @SuppressWarnings("rawtypes")
    public void setInitializeParams(Map params) {
        if (params != null) {
            setInitializeParams( new Object[] { params } );
        }
    }

    public Object[] getInitializeParams() {
        return this.initializeParams;
    }
    
    public void setAlwaysReload(boolean alwaysReload) {
        this.alwaysReload = alwaysReload;
    }
    
    public boolean isAlwaysReload() {
        return this.alwaysReload;
    }

    public void setComponentWrapperClass(Class<? extends AbstractRubyComponent> wrapperClass) {
        this.wrapperClass = wrapperClass;
    }

    public Class<? extends AbstractRubyComponent> getComponentWrapperClass() {
        return this.wrapperClass;
    }

    protected RubyComponent wrapComponent(IRubyObject rubyComponent) throws InstantiationException, IllegalAccessException {
        AbstractRubyComponent wrappedComponent = this.wrapperClass.newInstance();
        wrappedComponent.setRubyComponent( rubyComponent );
        return wrappedComponent;
    }

    private Class<? extends AbstractRubyComponent> wrapperClass;

    private ComponentInstantiator componentInstantiator;
    private String componentName;
    private Object[] initializeParams;
    private boolean alwaysReload = false;

}
