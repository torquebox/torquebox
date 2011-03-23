package org.torquebox.injection;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;

public class LoggerInjectable extends SimpleNamedInjectable {
    
    public static final String TYPE = "log";

    public LoggerInjectable(String name) {
        this( TYPE, name );
    }
    
    public LoggerInjectable(String type, String name) {
        super( type, name );
    }

    public ValueMetaData createMicrocontainerInjection(DeploymentUnit context, BeanMetaDataBuilder builder) {
        return builder.createValue( Logger.getLogger( getName() ) );
    }

}
