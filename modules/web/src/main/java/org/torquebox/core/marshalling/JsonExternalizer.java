package org.torquebox.core.marshalling;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class JsonExternalizer implements org.infinispan.marshall.Externalizer<IRubyObject> {

	private static final long serialVersionUID = 1L;
	private static Ruby ruby_ = null;
	private static HashMap<String, IRubyObject> serializers_ = new HashMap<String, IRubyObject>();

	@Override
	public IRubyObject readObject(ObjectInput input) throws IOException, ClassNotFoundException {
		IRubyObject theThing 			= null;
		String theType 					= (String) input.readObject();
		IRubyObject serializer 			= JsonExternalizer.getSerializer(theType);
		if (serializer != null) {
			// Deserialize the object
			String[] args = new String[1];
			args[0] = (String) input.readObject();
			theThing = (IRubyObject) JavaEmbedUtils.invokeMethod(getRuby(), serializer, "deserialize", args, IRubyObject.class);
		} else {
			System.err.println("[ERROR] Cannot deserialize " + theType + ". Assuming string");
		}
		return theThing;
	}

	@Override
	public void writeObject(ObjectOutput output, IRubyObject object)
			throws IOException {
		Object rubyClass = JavaEmbedUtils.invokeMethod(getRuby(), object, "class", null, Object.class);
		String theType = (String) JavaEmbedUtils.invokeMethod(getRuby(), rubyClass, "name", null, String.class);
		IRubyObject serializer = JsonExternalizer.getSerializer(theType);
		if (serializer != null) {
			// Serialize the type
			output.writeObject(theType);
			Object[] args = new Object[1];
			args[0] = object;
			String theThing = (String) JavaEmbedUtils.invokeMethod(getRuby(), serializer, "serialize", args, String.class);
			output.writeObject(theThing);
		} else {
			System.err.println("[ERROR] Cannot serialize " + theType);
		}
	}
	
	public static void setSerializer(String key, IRubyObject serializer) {
		System.err.println("[INFO] Setting infinispan serializer for Ruby type: " + key);
		JsonExternalizer.serializers_.put(key, serializer);
	}
	
	public static IRubyObject getSerializer(String key) {
		return JsonExternalizer.serializers_.get(key);
	}
	
	public static void setRuby(Ruby ruby) {
		JsonExternalizer.ruby_ = ruby;
	}
	
	public static Ruby getRuby() {
		return JsonExternalizer.ruby_;
	}

}
