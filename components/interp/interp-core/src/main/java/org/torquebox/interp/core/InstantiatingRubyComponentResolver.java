package org.torquebox.interp.core;

import java.util.Collection;
import java.util.Map;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.interp.spi.ComponentInitializer;
import org.jboss.logging.Logger;


public class InstantiatingRubyComponentResolver extends ManagedComponentResolver {

	private String rubyClassName;
	private String rubyRequirePath;
    private Object[] initializeParams = new Object[] {};

	private static final Logger log = Logger.getLogger(InstantiatingRubyComponentResolver.class);

	private ComponentInitializer componentInitializer;

	public InstantiatingRubyComponentResolver() {
	}

	public void setRubyClassName(String rubyClassName) {
		this.rubyClassName = rubyClassName;
	}

	public String getRubyClassName() {
		return this.rubyClassName;
	}

	public void setRubyRequirePath(String rubyRequirePath) {
		this.rubyRequirePath = rubyRequirePath;
	}

	public String getRubyRequirePath() {
		return this.rubyRequirePath;
	}

    public void setInitializeParams(Object[] initializeParams) {
        this.initializeParams = initializeParams;
    }
    public void setInitializeParams(Collection params) {
        setInitializeParams( params.toArray() );
    }
    public void setInitializeParams(Map params) {
        setInitializeParams( new Object[] { params } );
    }

    public Object[] getInitializeParams() {
        return this.initializeParams;
    }

	public void setComponentInitializer(ComponentInitializer componentInitializer) {
		this.componentInitializer = componentInitializer;
	}

	public ComponentInitializer getComponentInitializer() {
		return this.componentInitializer;
	}

	protected IRubyObject createComponent(Ruby ruby) throws Exception {
        log.debug("createComponent("+ruby+")");
		if (this.rubyRequirePath != null) {
			ruby.getLoadService().load(this.rubyRequirePath + ".rb", false);
            log.debug("Loaded source file: "+this.rubyRequirePath+".rb");
		}

		RubyClass componentClass = (RubyClass) ruby.getClassFromPath(this.rubyClassName);
        log.debug("Got componentClass: "+componentClass);
		if (componentClass == null || componentClass.isNil()) {
			return null;
		}

		IRubyObject component = (IRubyObject) JavaEmbedUtils.invokeMethod(ruby, componentClass, "new", getInitializeParams(), IRubyObject.class);
        log.debug("Got component: "+component);
		if (this.componentInitializer != null) {
			this.componentInitializer.initialize(component);
            log.debug("Initialized component");
		}

		return component;
	}

}
