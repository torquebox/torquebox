package org.torquebox.messaging.injection;

import java.util.Set;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.common.util.StringUtils;
import org.torquebox.injection.jndi.JNDIInjectable;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.AbstractDestinationMetaData;
import org.torquebox.messaging.QueueMetaData;
import org.torquebox.messaging.core.AbstractManagedDestination;
import org.torquebox.messaging.core.ManagedQueue;
import org.torquebox.messaging.core.ManagedTopic;

public class DestinationInjectable extends JNDIInjectable {
    
    public DestinationInjectable(String type, String name, boolean generic) {
        super( type, name, generic );
    }

    @Override
    public ValueMetaData createMicrocontainerInjection(DeploymentUnit context, BeanMetaDataBuilder builder) {
        Class<? extends AbstractManagedDestination> destinationClass = demandDestination( context, getName() );
        
        String rootDependencyName = null;
        
        if (destinationClass != null) {
            rootDependencyName = AttachmentUtils.beanName( context, destinationClass, getName() );
        } else {
            rootDependencyName = getTargetName();
        }
        
        String beanName = AttachmentUtils.beanName( context, DestinationInjection.class, getName() );
        BeanMetaDataBuilder injectionBuilder = BeanMetaDataBuilder.createBuilder( beanName, DestinationInjection.class.getName() );
        injectionBuilder.addConstructorParameter( String.class.getName(), StringUtils.camelize( getType() ) );
        injectionBuilder.addConstructorParameter( String.class.getName(), getName() );
        injectionBuilder.addDemand( rootDependencyName );
        AttachmentUtils.attach( context, injectionBuilder.getBeanMetaData() );
        return injectionBuilder.createInject( injectionBuilder.getBeanMetaData().getName() );
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
