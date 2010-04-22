/* Copyright 2010 Red Hat, Inc. */

package org.torquebox.common.reflect;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;

/** Ruby reflection helper utilities.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class ReflectionHelper {
	
	/** Set a property on a Ruby object, if possible.
	 * 
	 * <p>If the target responds to {@code name=}, the
	 * property will be set.  Otherwise, not.</p>
	 * 
	 * @param ruby The Ruby interpreter.
	 * @param target The target object.
	 * @param name The basic name of the property.
	 * @param value The value to attempt to set.
	 * @return {@code true} if successful, otherwise {@code false}
	 */
	public static boolean setIfPossible(Ruby ruby, Object target, String name, Object value) {
		boolean success = false;
		
		Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod( ruby, target, "respond_to?", new Object[] { name + "=" }, Boolean.class );
		
		if ( respondTo.booleanValue() ) {
			JavaEmbedUtils.invokeMethod( ruby, target, name + "=", new Object[]{ value }, void.class );
		}
		
		return success;
	}

}
