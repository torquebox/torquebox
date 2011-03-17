package org.torquebox.injection;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;

public class JNDIInjectable extends SimpleNamedInjectable {
    
    public static final String JNDI_NAME_PREFIX = "jndi:";
    
    public JNDIInjectable(String name) {
        this( "jndi", name );
    }
    
    public JNDIInjectable(String type, String name) {
        super( type, name );
    }

    @Override
    public ValueMetaData createMicrocontainerInjection(DeploymentUnit context, BeanMetaDataBuilder builder) {
        return builder.createInject( JNDI_NAME_PREFIX + getName() );
    }

}
