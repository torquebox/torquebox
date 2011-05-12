package org.torquebox.injection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.torquebox.core.injection.InjectionRegistry;
import org.torquebox.core.injection.analysis.Injectable;
import org.torquebox.mc.AttachmentUtils;

public class BaseRubyProxyInjectionBuilder {

    public static final String DEFAULT_INJECTION_REGISTRY_PROPERTY_NAME = "injectionRegistry";
    
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( BaseRubyProxyInjectionBuilder.class );

    private DeploymentUnit context;
    private BeanMetaDataBuilder beanBuilder;

    public BaseRubyProxyInjectionBuilder(DeploymentUnit context, BeanMetaDataBuilder beanBuilder) {
        this.context = context;
        this.beanBuilder = beanBuilder;
    }
    
    public DeploymentUnit getContext() {
        return this.context;
    }
    
    public BeanMetaDataBuilder getBeanMetaDataBuilder() {
        return this.beanBuilder;
    }
    
    public void injectInjectionRegistry(Collection<Injectable> injectables) {
        Map<ValueMetaData, ValueMetaData> registryMap = buildInjectableRegistryMap( injectables );
        String registryBeanName = this.beanBuilder.getBeanMetaData().getName() + "$" + InjectionRegistry.class.getName();
        
        BeanMetaDataBuilder registryBuilder = BeanMetaDataBuilder.createBuilder( registryBeanName, InjectionRegistry.class.getName() );
        registryBuilder.addPropertyMetaData( "injectionRegistry", registryMap );
        
        AttachmentUtils.attach(  this.context, registryBuilder.getBeanMetaData() );
        beanBuilder.addPropertyMetaData( DEFAULT_INJECTION_REGISTRY_PROPERTY_NAME, beanBuilder.createInject( registryBeanName ) );
    }

    public Map<ValueMetaData, ValueMetaData> buildInjectableRegistryMap(Collection<Injectable> injectables) {
        Map<ValueMetaData, ValueMetaData> registry = beanBuilder.createMap( HashMap.class.getName(), String.class.getName(), null );

        for (Injectable each : injectables) {
            ValueMetaData key = beanBuilder.createString( String.class.getName(), each.getKey() );
            ValueMetaData value = each.createMicrocontainerInjection( context, beanBuilder );
            
            registry.put(  key, value );
        }
        
        return registry;
    }
    


}
