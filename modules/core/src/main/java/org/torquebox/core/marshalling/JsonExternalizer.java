package org.torquebox.core.marshalling;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jboss.logging.Logger;
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
	    log.info( "BOB: readObject" );
		String theType = (String) input.readObject();
		log.info(">>>>>>>>>> LANCE: Reading object type: " + theType);
		String theJson = (String) input.readObject();
		return fromJSON(theJson, theType);	
	}

	@Override
	public void writeObject(ObjectOutput output, IRubyObject object)
			throws IOException {
	    log.info( "BOB: writeObject(..., " + object + ")" );
		//String theType = object.getJavaClass().getName();
		String theType = object.getType().getName();
		// Serialize the type & object
		log.info(">>>>>>>>>> LANCE: Writing Ruby object type: '" + theType + "'");
		output.writeObject(theType);
		log.info(">>>>>>>>>> LANCE: Converting object to json");
		String json = toJSON(object);
		log.info(">>>>>>>>>> LANCE: Writing object data");
		output.writeObject(json);
	}
	
    protected String toJSON(IRubyObject object) {
        log.info(">>>>>>>> LANCE: requiring json");
        RuntimeHelper.require( getCurrentRuntime(), "json" );
        log.info(">>>>>>>> LANCE: converting to json: " + object.asString());
        return (String) JavaEmbedUtils.invokeMethod( getCurrentRuntime(), object, "to_json", EMPTY_OBJECT_ARRAY, String.class );
    }

    protected IRubyObject fromJSON(String json, String type) throws ClassNotFoundException {
        Ruby runtime = getCurrentRuntime();
        RuntimeHelper.require( runtime, "json" );
        RubyModule jsonClass = runtime.getClassFromPath( "JSON" );
        log.info(  "BOB: jsonClass=" + jsonClass );
        if ( jsonClass == null ) {
            throw new ClassNotFoundException( "JSON" );
        }
        RubyHash jsonHash = (RubyHash) JavaEmbedUtils.invokeMethod( runtime, jsonClass, "parse", new Object[] { json }, RubyHash.class );
        log.info(  "BOB: jsonHash=" + jsonHash );
        RubyModule objectClass = runtime.getClassFromPath( type );
        return (IRubyObject) JavaEmbedUtils.invokeMethod( runtime, objectClass, "new", new Object[] { jsonHash }, IRubyObject.class); 
    }
    
    protected Ruby getCurrentRuntime() {
        return RuntimeContext.getCurrentRuntime();
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.core.marshalling.json" );
}
