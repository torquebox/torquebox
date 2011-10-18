package org.torquebox.stomp.injection;

import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.torquebox.core.injection.SimpleNamedInjectable;
import org.torquebox.stomp.RubyStompletMetaData;
import org.torquebox.stomp.as.StompServices;

public class StompEndpointBindingInjectable extends SimpleNamedInjectable {
    
    public static final StompEndpointBindingInjectable INSTANCE = new StompEndpointBindingInjectable();

    public StompEndpointBindingInjectable() {
        super( "stomp-endpoint", "stomp-endpoint", false, true );
    }

    @Override
    public ServiceName getServiceName(ServiceTarget serviceTarget, DeploymentUnit unit) throws Exception {
        AttachmentList<RubyStompletMetaData> stomplets = unit.getAttachment( RubyStompletMetaData.ATTACHMENTS_KEY );
        
        if ( stomplets == null || stomplets.isEmpty() ) {
            return null;
        }
        
        return StompServices.endpointBinding( unit );
    }

}
