package org.torquebox.injection;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;

public class SimpleNamedInjectable implements Injectable {
    
    private String type;
    private String name;
    private boolean generic;

    public SimpleNamedInjectable(String type, String name, boolean generic) {
        this.type = type;
        this.name = name;
        this.generic = generic;
    }
    
    public boolean isGeneric() {
        return this.generic;
    }
    
    public String getType() {
        return this.type;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getKey() {
        return getName();
    }
    
    @Override
    public ValueMetaData createMicrocontainerInjection(DeploymentUnit context, BeanMetaDataBuilder builder) {
        return builder.createInject( getName() );
    }
    
    public String toString() {
        return "[" + getClass().getName() + ": " + this.name + "]";
    }


}
