package org.torquebox.ruby.enterprise.queues;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimePool;

public class BaseRubyMessageListener implements MessageListener {
	private static final Object[] EMPTY_OBJECT_ARRAY = {};

	private static final Logger log = Logger.getLogger(BaseRubyMessageListener.class);
	private RubyRuntimePool pool;
	private String queueClassName;
	private String classLocation;

	public BaseRubyMessageListener(RubyRuntimePool pool, String queueClassName, String classLocation) {
		this.pool = pool;
		this.queueClassName = queueClassName;
		this.classLocation = classLocation;
	}

	public void onMessage(Message message) {

		Ruby ruby = null;

		try {
			ruby = this.pool.borrowRuntime();

			loadQueueClassLocation(ruby);

			RubyModule queueClass = ruby.getClassFromPath(this.queueClassName);
			IRubyObject rubyQueue = (IRubyObject) JavaEmbedUtils.invokeMethod(ruby, queueClass, "new", EMPTY_OBJECT_ARRAY, Object.class);
			
			String taskName = message.getStringProperty("TaskName");

			Object payload = ((ObjectMessage) message).getObject();

			if (message.getBooleanProperty("IsRubyMarshal")) {
				RubyModule marshal = ruby.getClassFromPath("Marshal");
				payload = JavaEmbedUtils.invokeMethod(ruby, marshal, "restore", new Object[] { payload }, Object.class);
			}
			
			injectLogger(rubyQueue);

			JavaEmbedUtils.invokeMethod(ruby, rubyQueue, taskName, new Object[] { payload }, void.class);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (ruby != null) {
				this.pool.returnRuntime(ruby);
			}
		}

	}

	private void loadSupport(Ruby ruby) {
		String load = "require %q(torquebox/queues/base)\n";
		ruby.evalScriptlet(load);
	}
	
	private void injectLogger(IRubyObject rubyQueue) {
		boolean isInjectable = ((Boolean) JavaEmbedUtils.invokeMethod(rubyQueue.getRuntime(), rubyQueue, "respond_to?",
				new Object[] { "log=" }, Boolean.class)).booleanValue();

		if (isInjectable) {
			String loggerName = queueClassName;
			Logger logger = Logger.getLogger(loggerName);
			JavaEmbedUtils.invokeMethod(rubyQueue.getRuntime(), rubyQueue, "log=", new Object[] { logger }, void.class);
		} else {
			log.warn("Unable to inject log into " + queueClassName);
		}
	}

	private void loadQueueClassLocation(Ruby ruby) {
		if (this.classLocation == null) {
			return;
		}
		loadSupport(ruby);
		String load = "load %q(" + this.classLocation + ".rb)\n";
		ruby.evalScriptlet(load);
	}

}
