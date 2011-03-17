package org.torquebox.messaging.injection;

import java.util.Set;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.injection.JNDIInjectable;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.core.AbstractManagedDestination;
import org.torquebox.messaging.core.ManagedQueue;
import org.torquebox.messaging.core.ManagedTopic;
import org.torquebox.messaging.metadata.AbstractDestinationMetaData;
import org.torquebox.messaging.metadata.QueueMetaData;

public class DestinationInjectable extends JNDIInjectable {
    
    public DestinationInjectable(String type, String name) {
        super( type, name );
    }

    @Override
    public ValueMetaData createMicrocontainerInjection(DeploymentUnit context, BeanMetaDataBuilder builder) {
        Class<? extends AbstractManagedDestination> destinationClass = demandDestination( context, getName() );
        
        if (destinationClass != null) {
            String destinationBeanName = AttachmentUtils.beanName( context, destinationClass, getName() );
            return builder.createInject( destinationBeanName, null, ControllerState.CREATE, ControllerState.INSTALLED );
        } else {
            return super.createMicrocontainerInjection( context, builder );
        }
    }
    
    protected Class<? extends AbstractManagedDestination> demandDestination(DeploymentUnit unit, String destinationName) {
        Set<? extends AbstractDestinationMetaData> destinations = unit.getAllMetaData( AbstractDestinationMetaData.class );

        for (AbstractDestinationMetaData each : destinations) {
            if (each.getName().equals( destinationName )) {
                if (each.getClass() == QueueMetaData.class) {
                    return ManagedQueue.class;
                } else {
                    return ManagedTopic.class;
                }
            }
        }
        return null;
    }
    
    

}
