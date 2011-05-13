package org.torquebox.core.component;

import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class AbstractRubyComponent implements RubyComponent {
    
    public AbstractRubyComponent() {
    }
    
    void setRubyComponent(IRubyObject rubyComponent) {
        this.rubyComponent = rubyComponent;
    }
    
    public void applyInjections(Injections injections) {
        
    }
    
    protected Object __call__(String method, Object...args) {
        return JavaEmbedUtils.invokeMethod(  this.rubyComponent.getRuntime(), this.rubyComponent, method, args, Object.class );
    }
    
    private ComponentResolver componentResolver;
    private IRubyObject rubyComponent;


}
