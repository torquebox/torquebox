package org.torquebox.injection;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;

public interface Injectable {
    
    String getName();
    ValueMetaData createMicrocontainerInjection(DeploymentUnit context, BeanMetaDataBuilder builder);

}
