package org.torquebox.injection.cdi;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.weld.integration.deployer.DeployersUtils;
import org.torquebox.core.injection.SimpleNamedInjectable;
import org.torquebox.mc.AttachmentUtils;

public class CDIInjectable extends SimpleNamedInjectable {

    public CDIInjectable(String name, boolean generic) {
        super( "cdi", name, generic);
    }

    @Override
    public ValueMetaData createMicrocontainerInjection(DeploymentUnit context, BeanMetaDataBuilder builder) {
        
        String cdiBridgeBeanName = AttachmentUtils.beanName( context, CDIBridge.class  );
        String injectableBridgeBeanName = AttachmentUtils.beanName( context, CDIInjectionBridge.class, "" + this.counter.getAndIncrement() );
        
        BeanMetaDataBuilder injectionBridgeBuilder = BeanMetaDataBuilder.createBuilder( injectableBridgeBeanName, CDIInjectionBridge.class.getName() );
        injectionBridgeBuilder.addConstructorParameter( CDIBridge.class.getName(), builder.createInject( cdiBridgeBeanName ) );
        injectionBridgeBuilder.addConstructorParameter( String.class.getName(), getName() );
        
        String bootstrapName = DeployersUtils.getBootstrapBeanName(context);
        injectionBridgeBuilder.addDemand(  bootstrapName );
        injectionBridgeBuilder.addDemand(  FallbackBeanManagerJndiBinder.class.getSimpleName() );
        
        AttachmentUtils.attach( context, injectionBridgeBuilder.getBeanMetaData() );
        
        return builder.createInject( injectableBridgeBeanName, "value" );
    }

    @Override
    public String getKey() {
        String fullName = getName();

        int lastDot = fullName.lastIndexOf( '.' );

        String className = null;
        String packageName = null;

        if (lastDot > 0) {
            className = fullName.substring( lastDot + 1 );
            packageName = fullName.substring( 0, lastDot );
        } else {
            className = fullName;
        }
        return "Java::" + getPackageKeyPart( packageName ) + "::" + className;

    }

    public String getPackageKeyPart(String packageName) {
        if ( packageName == null ) {
            return "Default";
        }
        
        int length = packageName.length();

        StringBuilder buf = new StringBuilder();
        
        for (int start = 0, offset = 0; start < length; start = offset + 1) {
            if ((offset = packageName.indexOf( '.', start )) == -1) {
                offset = length;
            }
            buf.append( Character.toUpperCase( packageName.charAt( start ) ) ).append( packageName.substring( start + 1, offset ) );
        }
        return buf.toString();
    }
    
    private AtomicInteger counter = new AtomicInteger();

}
