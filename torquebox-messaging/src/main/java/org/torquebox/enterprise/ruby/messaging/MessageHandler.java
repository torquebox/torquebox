package org.torquebox.enterprise.ruby.messaging;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.common.reflect.ReflectionHelper;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimePool;

public class MessageHandler implements MessageListener {
	
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};

	private Session session;
	private RubyRuntimePool runtimePool;
	
	private String rubyClassName;

	public MessageHandler() {
		
	}
	
	public String toString() {
		return "[MessageHandler: rubyClassName=" + rubyClassName + "]";
	}
	
	void setSession(Session session) {
		this.session = session;
	}
	
	public Session getSession() {
		return this.session;
	}
	
	public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
		this.runtimePool = runtimePool;
	}
	
	public RubyRuntimePool getRubyRuntimePool() {
		return this.runtimePool;
	}
	
	public void setRubyClassName(String rubyClassName) {
		this.rubyClassName = rubyClassName;
	}
	
	public String getRubyClassName() {
		return this.rubyClassName;
	}

	@Override
	public void onMessage(Message message) {
		Ruby ruby = null;
		
		try {
			ruby = getRubyRuntimePool().borrowRuntime();
			RubyClass rubyClass = (RubyClass) ruby.getClassFromPath( getRubyClassName() );
			IRubyObject listener = (IRubyObject) JavaEmbedUtils.invokeMethod( ruby, rubyClass, "new", EMPTY_OBJECT_ARRAY, IRubyObject.class );
			
			ReflectionHelper.setIfPossible( ruby, listener, "session", getSession() );
			
			JavaEmbedUtils.invokeMethod( ruby, listener, "on_message", new Object[] { message }, void.class);
			message.acknowledge();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if ( ruby != null ) {
				getRubyRuntimePool().returnRuntime( ruby );
			}
		}
	}
	

}
