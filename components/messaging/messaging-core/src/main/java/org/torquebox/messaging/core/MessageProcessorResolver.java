package org.torquebox.messaging.core;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.interp.core.ManagedComponentResolver;

public class MessageProcessorResolver extends ManagedComponentResolver {

	private String key;
	
	public MessageProcessorResolver(String key) {
		this.key = key;
		setComponentName( "Torquebox.MessageProcessor." + key );
	}
	
	@Override
	protected IRubyObject createComponent(Ruby ruby) throws Exception {
		RubyClass configClass = (RubyClass) ruby.getClassFromPath("TorqueBox::Messaging::Config");
		
		return null;
	}

}
