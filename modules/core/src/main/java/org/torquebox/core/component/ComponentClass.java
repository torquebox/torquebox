package org.torquebox.core.component;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class ComponentClass implements ComponentInstantiator {

    private String className;
    private String requirePath;

    public ComponentClass() {

    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    public void setRequirePath(String requirePath) {
        this.requirePath = requirePath;
    }

    public String getRequirePath() {
        return this.requirePath;
    }

    public RubyModule getComponentClass(Ruby runtime) {
        if (this.requirePath != null) {
            runtime.getLoadService().load( this.requirePath + ".rb", false );
        }

        RubyModule componentClass = (RubyModule) runtime.getClassFromPath( this.className );

        if (componentClass == null || componentClass.isNil()) {
            return null;
        }

        return componentClass;
    }

    public IRubyObject newInstance(Ruby runtime, Object[] initParams) {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader( runtime.getJRubyClassLoader().getParent() );
            RubyModule rubyClass = getComponentClass( runtime );
            IRubyObject component = (IRubyObject) JavaEmbedUtils.invokeMethod( runtime, rubyClass, "new", initParams, IRubyObject.class );
            return component;
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );

        }
    }

}
