package org.torquebox.common.reflect;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;

public class ReflectionHelper {
	
	public static boolean setIfPossible(Ruby ruby, Object target, String name, Object value) {
		boolean success = false;
		
		Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod( ruby, target, "respond_to?", new Object[] { name + "=" }, Boolean.class );
		
		if ( respondTo.booleanValue() ) {
			JavaEmbedUtils.invokeMethod( ruby, target, name + "=", new Object[]{ value }, void.class );
		}
		
		return success;
	}

}
