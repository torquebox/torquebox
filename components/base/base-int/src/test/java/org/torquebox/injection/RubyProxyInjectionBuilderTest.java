package org.torquebox.injection;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.injection.mc.MCBeanInjectable;
import org.torquebox.injection.spi.InjectableRegistry;
import org.torquebox.injection.spi.RubyInjectionProxy;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class RubyProxyInjectionBuilderTest extends AbstractDeployerTestCase {
    
    private Object mcbean;

    @Before
    public void setUp() throws Throwable {
        this.mcbean = new Object();
        BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(  "mcbean", Object.class.getName() );
        getKernelController().install( builder.getBeanMetaData(), this.mcbean );
    }
    
    @Test
    public void testItShouldInjectCorrectly() throws IOException, DeploymentException {
        
        BeanMetaDataBuilder beanBuilder = BeanMetaDataBuilder.createBuilder( "proxy", MockProxy.class.getName() );
        
        List<Injectable> injectables = new ArrayList<Injectable>();
        injectables.add( new MCBeanInjectable( "mcbean", false  ) );
        
        String deploymentName = createDeployment( "test-deployment" );
        DeploymentUnit unit = getDeploymentUnit(  deploymentName  );
        
        RubyProxyInjectionBuilder injectionBuilder = new RubyProxyInjectionBuilder( unit, null, beanBuilder );
        injectionBuilder.addInjectableRegistry( injectables );
        
        System.err.println( "BEANS: " + beanBuilder.getBeanMetaDataFactory().getBeans().size() );
        AttachmentUtils.attach(  unit, beanBuilder.getBeanMetaDataFactory() );
        
        System.err.println(  "AAA" );
        processDeployments(true);
        
        MockProxy proxy = (MockProxy) getBean( "proxy" );
        
        System.err.println(  "BBB" );
        assertNotNull( proxy );
        assertNotNull( proxy.registry );
        assertSame( this.mcbean, proxy.registry.get(  "mc", "mcbean" ));
        System.err.println(  "CCC" );
    }
    
    public static class MockProxy implements RubyInjectionProxy {

        public InjectableRegistry registry;

        @Override
        public void setInjectableRegistry(InjectableRegistry injectionRegistry) {
            System.err.println( "setInjectableRegistry(" + injectionRegistry + ")" );
            this.registry = injectionRegistry;
            
        }
        
    }

}
