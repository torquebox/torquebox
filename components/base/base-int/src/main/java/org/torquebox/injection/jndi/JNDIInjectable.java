package org.torquebox.injection.jndi;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.injection.SimpleNamedInjectable;
import org.torquebox.mc.JNDIKernelRegistryPlugin;

public class JNDIInjectable extends SimpleNamedInjectable {
    
    public JNDIInjectable(String name, boolean generic) {
        this( "jndi", name, generic );
    }
    
    protected JNDIInjectable(String type, String name, boolean generic) {
        super( type, name, generic );
    }
    
    
    @Override
    public ValueMetaData createMicrocontainerInjection(DeploymentUnit context, BeanMetaDataBuilder builder) {
        return builder.createInject( getTargetName() );
    }
    
    protected String getTargetName() {
        return JNDIKernelRegistryPlugin.JNDI_DEPENDENCY_PREFIX + getName();
    }

}
