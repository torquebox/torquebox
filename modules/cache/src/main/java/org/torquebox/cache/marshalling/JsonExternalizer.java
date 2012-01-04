/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.cache.marshalling;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.infinispan.marshall.Externalizer;
import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.core.runtime.RuntimeContext;
import org.torquebox.core.util.RuntimeHelper;

public class JsonExternalizer implements Externalizer<IRubyObject> {

	private static final long serialVersionUID = 1L;
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};


	@Override
	public IRubyObject readObject(ObjectInput input) throws IOException, ClassNotFoundException {
		String theType = (String) input.readObject();
		String theJson = (String) input.readObject();
		return fromJSON(theJson, theType);	
	}

	@Override
	public void writeObject(ObjectOutput output, IRubyObject object)
			throws IOException {
		String theType = object.getType().getName();
		output.writeObject(theType);
		String json = toJSON(object);
		output.writeObject(json);
	}
	
    protected String toJSON(IRubyObject object) {
        RuntimeHelper.require( object.getRuntime(), "json" );
        return (String) JavaEmbedUtils.invokeMethod( object.getRuntime(), object, "to_json", EMPTY_OBJECT_ARRAY, String.class );
    }

    protected IRubyObject fromJSON(String json, String type) throws ClassNotFoundException {
        Ruby runtime = getCurrentRuntime();
        RuntimeHelper.require( runtime, "json" );
        RubyModule jsonClass = runtime.getClassFromPath( "JSON" );
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
