package org.torquebox.interp.core;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.interp.spi.ComponentInitializer;

public class InstantiatingRubyComponentResolver extends ManagedComponentResolver {

	private String rubyClassName;
	private String rubyRequirePath;

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

	public void setComponentInitializer(ComponentInitializer componentInitializer) {
		this.componentInitializer = componentInitializer;
	}

	public ComponentInitializer getComponentInitializer() {
		return this.componentInitializer;
	}

	protected IRubyObject createComponent(Ruby ruby) throws Exception {
		if (this.rubyRequirePath != null) {
			ruby.getLoadService().load(this.rubyRequirePath + ".rb", false);
		}

		RubyClass componentClass = (RubyClass) ruby.getClassFromPath(this.rubyClassName);

		if (componentClass == null || componentClass.isNil()) {
			return null;
		}

		IRubyObject component = (IRubyObject) JavaEmbedUtils.invokeMethod(ruby, componentClass, "new", new Object[] {}, IRubyObject.class);

		if (this.componentInitializer != null) {
			this.componentInitializer.initialize(component);
		}

		return component;
	}

}
