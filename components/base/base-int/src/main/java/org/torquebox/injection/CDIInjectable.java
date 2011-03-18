package org.torquebox.injection;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.mc.CDIKernelRegistryPlugin;

public class CDIInjectable extends SimpleNamedInjectable {
    
    public CDIInjectable(String name) {
        this( "cdi", name );
    }
    
    public CDIInjectable(String type, String name) {
        super( type, name );
    }

    @Override
    public ValueMetaData createMicrocontainerInjection(DeploymentUnit context, BeanMetaDataBuilder builder) {
        return builder.createInject( CDIKernelRegistryPlugin.CDI_DEPENDENCY_PREFIX + getName() );
    }

}
