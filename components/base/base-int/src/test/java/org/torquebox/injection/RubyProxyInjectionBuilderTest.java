package org.torquebox.injection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.injection.mc.MCBeanInjectable;
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
        
        BaseRubyProxyInjectionBuilder injectionBuilder = new BaseRubyProxyInjectionBuilder( unit, beanBuilder );
        injectionBuilder.injectInjectionRegistry( injectables );
        
        AttachmentUtils.attach(  unit, beanBuilder.getBeanMetaDataFactory() );
        
        processDeployments(true);
        
        MockProxy proxy = (MockProxy) getBean( "proxy" );
        
        assertNotNull( proxy );
        assertNotNull( proxy.registry );
        assertSame( this.mcbean, proxy.registry.getUnconverted( "mcbean" ));
    }
    
    public static class MockProxy implements RubyInjectionProxy {

        public InjectionRegistry registry;

        @Override
        public void setInjectionRegistry(InjectionRegistry registry) {
            this.registry = registry;
        }
        
    }

}
