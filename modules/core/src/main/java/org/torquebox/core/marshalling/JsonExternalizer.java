package org.torquebox.core.marshalling;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.core.runtime.RuntimeContext;
import org.torquebox.core.util.RuntimeHelper;

public class JsonExternalizer implements org.infinispan.marshall.Externalizer<IRubyObject> {

	private static final long serialVersionUID = 1L;
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};


	@Override
	public IRubyObject readObject(ObjectInput input) throws IOException, ClassNotFoundException {
		String theType = (String) input.readObject();
		String theJson = (String) input.readObject();
		System.err.println(">>>>>>>>>> LANCE: Reading object type: " + theType);
		return fromJSON(theJson, theType);	
	}

	@Override
	public void writeObject(ObjectOutput output, IRubyObject object)
			throws IOException {
		String theType = object.getJavaClass().getName();
		System.err.println(">>>>>>>>>> LANCE: Writing object type: " + theType);
		// Serialize the type & object
		output.writeObject(theType);
		System.err.println(">>>>>>>>>> LANCE: Writing object data");
		output.writeObject(toJSON(object));
	}
	
    protected String toJSON(IRubyObject object) {
        System.err.println(">>>>>>>> LANCE: getting current runtime");
    	Ruby runtime = getCurrentRuntime();
        System.err.println(">>>>>>>> LANCE: requiring json");
        System.err.println(">>>>>>>> LANCE: using runtime: " + runtime);
        RuntimeHelper.require( runtime, "json" );
        System.err.println(">>>>>>>> LANCE: converting to json: " + object.asString());
        System.err.println(">>>>>>>> LANCE: using runtime: " + runtime);
        return (String) JavaEmbedUtils.invokeMethod( runtime, object, "to_json", EMPTY_OBJECT_ARRAY, String.class );
    }

    protected IRubyObject fromJSON(String json, String type) {
        Ruby runtime = getCurrentRuntime();
        RuntimeHelper.require( runtime, "json" );
        RubyModule jsonClass = runtime.getClass( "JSON" );
        RubyHash jsonHash = (RubyHash) JavaEmbedUtils.invokeMethod( runtime, jsonClass, "parse", new Object[] { json }, RubyHash.class );
        RubyModule objectClass = runtime.getClassFromPath( type );
        return (IRubyObject) JavaEmbedUtils.invokeMethod( runtime, objectClass, "new", new Object[] { jsonHash }, IRubyObject.class); 
    }
    
    protected Ruby getCurrentRuntime() {
        return RuntimeContext.getCurrentRuntime();
    }
}
