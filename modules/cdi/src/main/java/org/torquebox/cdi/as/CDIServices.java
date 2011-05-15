package org.torquebox.cdi.as;

import org.jboss.msc.service.ServiceName;
import org.torquebox.core.as.CoreServices;

public class CDIServices {

    private CDIServices() {
    }

    public static final ServiceName MESSAGING = CoreServices.TORQUEBOX.append( "messaging" );
    
}
