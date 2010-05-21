package org.torquebox.interp.core;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public abstract class ManagedComponentResolver implements RubyComponentResolver {

	private String componentName;

	public ManagedComponentResolver() {
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getComponentName() {
		return this.componentName;
	}

	@Override
	public IRubyObject resolve(Ruby ruby) throws Exception {
		synchronized (ruby) {
			ruby.getLoadService().require("org/torquebox/interp/core/component_manager");
			RubyClass managerClass = (RubyClass) ruby.getClassFromPath("TorqueBox::ComponentManager");
			IRubyObject component = (IRubyObject) JavaEmbedUtils.invokeMethod(ruby, managerClass, "lookup_component", new Object[] { this.componentName }, IRubyObject.class);

			if (component == null || component.isNil()) {
				component = createComponent(ruby);
				if (component != null) {
					JavaEmbedUtils.invokeMethod(ruby, managerClass, "register_component", new Object[] { this.componentName, component }, void.class);
				}
			}

			return component;
		}
	}
	
	protected abstract IRubyObject createComponent(Ruby ruby) throws Exception;

}
