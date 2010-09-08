/* Copyright 2010 Red Hat, Inc. */

package org.torquebox.common.reflect;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * Ruby reflection helper utilities.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class ReflectionHelper {

	/**
	 * Set a property on a Ruby object, if possible.
	 * 
	 * <p>
	 * If the target responds to {@code name=}, the property will be set.
	 * Otherwise, not.
	 * </p>
	 * 
	 * @param ruby
	 *            The Ruby interpreter.
	 * @param target
	 *            The target object.
	 * @param name
	 *            The basic name of the property.
	 * @param value
	 *            The value to attempt to set.
	 * @return {@code true} if successful, otherwise {@code false}
	 */
	public static boolean setIfPossible(Ruby ruby, Object target, String name, Object value) {
		boolean success = false;

		Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod(ruby, target, "respond_to?", new Object[] { name + "=" }, Boolean.class);

		if (respondTo.booleanValue()) {
			JavaEmbedUtils.invokeMethod(ruby, target, name + "=", new Object[] { value }, void.class);
		}

		return success;
	}
	
	public static Object getIfPossible(Ruby ruby, Object target, String name) {
		Object result = null;
		
		Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod(ruby, target, "respond_to?", new Object[] { name }, Boolean.class);
		
		// System.err.println( "OBJ: " + target );
		// System.err.println( "OBJ.class: " + target.getClass() );
		// System.err.println( "name: " + name );
		// System.err.println( "respondTo?" + respondTo );

		if (respondTo.booleanValue()) {
			result = JavaEmbedUtils.invokeMethod(ruby, target, name, new Object[] {}, Object.class);
		}	
		
		
		// System.err.println( "RESULT: " + result );
		return result;
	}

	public static Object callIfPossible(Ruby ruby, Object target, String name, Object[] parameters) {
		Object result = null;

		Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod(ruby, target, "respond_to?", new Object[] { name }, Boolean.class);

		if (respondTo.booleanValue()) {
			result = JavaEmbedUtils.invokeMethod(ruby, target, name, parameters, Object.class);
		}

		return result;
	}

	public static IRubyObject instantiate(Ruby ruby, String className) {
		return instantiate(ruby, className, new Object[] {});
	}

	public static IRubyObject instantiate(Ruby ruby, String className, Object[] parameters) {
		IRubyObject result = null;
		RubyModule rubyClass = ruby.getClassFromPath(className);

		if (rubyClass != null) {
			result = (IRubyObject) JavaEmbedUtils.invokeMethod(ruby, rubyClass, "new", parameters, IRubyObject.class);
		}
		
		return result;
	}

}
