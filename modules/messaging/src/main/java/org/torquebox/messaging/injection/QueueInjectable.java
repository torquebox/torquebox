package org.torquebox.messaging.injection;

import org.jboss.as.ee.naming.ContextNames;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.SimpleNamedInjectable;


public class QueueInjectable extends SimpleNamedInjectable {
    
    public QueueInjectable(String name, boolean generic) {
        this( "queue", name, generic );
    }
    
    protected QueueInjectable(String type, String name, boolean generic) {
        super( type, name, generic );
    }

    @Override
    public ServiceName getServiceName() {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( getName() );
    }

}
