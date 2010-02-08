package org.torquebox.ruby.enterprise.messaging;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimePool;

public class RubyMessageListener implements MessageListener {
	
	private static Logger log = Logger.getLogger( RubyMessageListener.class );
	private RubyRuntimePool pool;
	private String code;

	public RubyMessageListener() {
		
	}
	
	public void setRubyRuntimePool(RubyRuntimePool pool) {
		this.pool = pool;
	}
	
	public RubyRuntimePool getRubyRuntimePool() {
		return this.pool;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	@Override
	public void onMessage(Message message) {
		
		Ruby runtime = null;
		try {
			runtime = pool.borrowRuntime();
			runtime.evalScriptlet( "require %q(org/torquebox/ruby/enterprise/messaging/ruby_message_listener_bridge)" );
			RubyClass listenerClass = (RubyClass) runtime.getClassFromPath( "::TorqueBox::Messaging::RubyMessageListenerBridge" );
			IRubyObject listener = (IRubyObject) JavaEmbedUtils.invokeMethod( runtime, listenerClass, "new", new Object[] { getCode() }, IRubyObject.class );
			JavaEmbedUtils.invokeMethod(runtime, listener, "handle", new Object[] { message }, void.class );
		} catch (Exception e) {
			log.error( e );
		} finally {
			if ( runtime != null ) {
				pool.returnRuntime( runtime );
				pool = null;
			}
		}
	}

}
