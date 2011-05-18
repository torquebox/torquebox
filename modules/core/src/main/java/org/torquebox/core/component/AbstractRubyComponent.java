package org.torquebox.core.component;

import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class AbstractRubyComponent implements RubyComponent {
    
    public AbstractRubyComponent() {
    }
    
    public void setOptions(Map<String,Object> options) {
        this.options = options;
    }
    
    public Map<String,Object> getOptions() {
        return this.options;
    }
    
    public Object getOption(String name) {
        return this.options.get( name );
    }
    
    void setRubyComponent(IRubyObject rubyComponent) {
        this.rubyComponent = rubyComponent;
    }
    
    protected Object _callRubyMethod(Object target, String method, Object...args) {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader( getRuby().getJRubyClassLoader().getParent() );
            return JavaEmbedUtils.invokeMethod( this.rubyComponent.getRuntime(), target, method, args, Object.class );
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
        }
    }
    
    protected Object _callRubyMethod(String method, Object...args) {
    	return _callRubyMethod( this.rubyComponent, method, args );
    }
    
    protected RubyModule getClass(String path) {
        return this.rubyComponent.getRuntime().getClassFromPath( path );
    }
    
    protected Ruby getRuby() {
        return this.rubyComponent.getRuntime();
    }
    
    private Map<String, Object> options;
    private IRubyObject rubyComponent;
}
