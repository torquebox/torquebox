package org.torquebox.injection;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;

public interface Injectable {
    
    String getType();
    String getName();
    String getKey();
    ValueMetaData createMicrocontainerInjection(DeploymentUnit context, BeanMetaDataBuilder builder);
    boolean isGeneric();

}
