package org.torquebox.interp.core;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.interp.spi.ComponentInitializer;

public class InstantiatingRubyComponentResolver implements RubyComponentResolver {

	private String componentName;

	private String rubyClassName;
	private String rubyRequirePath;

	private ComponentInitializer componentInitializer;

	public InstantiatingRubyComponentResolver() {
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getComponentName() {
		return this.componentName;
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

	@Override
	public IRubyObject resolve(Ruby ruby) throws Exception {
		synchronized (ruby) {
			ruby.getLoadService().require("org/torquebox/interp/core/component_manager");
			RubyClass managerClass = (RubyClass) ruby.getClassFromPath("TorqueBox::ComponentManager");
			IRubyObject component = (IRubyObject) JavaEmbedUtils.invokeMethod(ruby, managerClass, "lookup_component", new Object[] { this.componentName }, IRubyObject.class);

			if (component == null || component.isNil()) {
				component = createComponent(ruby);
				JavaEmbedUtils.invokeMethod(ruby, managerClass, "register_component", new Object[] { this.componentName, component }, void.class);
			}

			return component;
		}
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
