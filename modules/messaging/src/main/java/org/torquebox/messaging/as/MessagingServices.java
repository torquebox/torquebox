package org.torquebox.messaging.as;

import org.jboss.msc.service.ServiceName;
import org.torquebox.core.as.CoreServices;

public class MessagingServices {

    private MessagingServices() {
    }

    public static final ServiceName MESSAGING = CoreServices.TORQUEBOX.append( "messaging" );
    
}
