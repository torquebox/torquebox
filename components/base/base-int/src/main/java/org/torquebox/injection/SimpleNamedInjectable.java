package org.torquebox.injection;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;

public class SimpleNamedInjectable implements Injectable {
    
    private String name;

    public SimpleNamedInjectable(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    @Override
    public ValueMetaData createMicrocontainerInjection(DeploymentUnit context, BeanMetaDataBuilder builder) {
        return builder.createInject( getName() );
    }
    
    public String toString() {
        return "[" + getClass().getName() + ": " + this.name + "]";
    }


}
