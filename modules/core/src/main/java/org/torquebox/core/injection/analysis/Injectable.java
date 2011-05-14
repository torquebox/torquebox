package org.torquebox.core.injection.analysis;

import org.jboss.msc.service.ServiceName;


public interface Injectable {
    
    String getType();
    String getName();
    String getKey();
    boolean isGeneric();
    
    ServiceName getServiceName();

}
