package org.torquebox.messaging.injection;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.torquebox.injection.ConvertableRubyInjection;

public class DestinationInjection implements ConvertableRubyInjection {


    public DestinationInjection(String type, String name) {
        this.type = type;
        this.name = name;
    }
    
    @Override
    public Object convert(Ruby ruby) {
        ruby.evalScriptlet(  "require %q(torquebox-messaging)"  );
        RubyModule destinationClass = ruby.getClassFromPath( "TorqueBox::Messaging::" + this.type );
        Object destination = JavaEmbedUtils.invokeMethod( ruby, destinationClass, "new", new Object[] { this.name }, Object.class );
        return destination;
    }
    
    private String type;
    private String name;

}
