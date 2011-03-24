package org.torquebox.injection.deployers;

import java.util.Set;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.beans.metadata.spi.builder.ParameterMetaDataBuilder;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.weld.integration.deployer.DeployersUtils;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.injection.cdi.FallbackBeanManagerJndiBinder;

/** Deployer that notices a Weld bootstrap, and wires up our FallbackBeanManagerJndiBinder 
 *  to catch non-JavaEE-alike deployments.
 *  
 *  Such as service-only knobs.
 *  
 *  @see FallbackBeanManagerJndiBinder#bind(DeploymentUnit)
 *  
 *  @author Bob McWhirter
 *
 */
public class FallbackBeanManagerJndiBinderDeployer extends AbstractDeployer {
    
    public FallbackBeanManagerJndiBinderDeployer() {
        setStage( DeploymentStages.PRE_REAL );
        setInput( RubyApplicationMetaData.class );
        addInput( BeanMetaData.class );
        addOutput( BeanMetaData.class );
        setTopLevelOnly( true );
        setRelativeOrder( 10000 );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        String bootstrapAttachmentName = DeployersUtils.getBootstrapBeanAttachmentName(unit);
        BeanMetaData bootstrapBmd = unit.getAttachment( bootstrapAttachmentName, BeanMetaData.class );
        
        if ( bootstrapBmd == null ) {
            return;
        }
        
        BeanMetaDataBuilder bootstrap = BeanMetaDataBuilder.createBuilder( bootstrapBmd );
        
        ParameterMetaDataBuilder jndiInstall = bootstrap.addInstallWithParameters("bind", FallbackBeanManagerJndiBinder.class.getSimpleName(), ControllerState.INSTALLED, ControllerState.START);
        jndiInstall.addParameterMetaData(DeploymentUnit.class.getName(), unit);

        ParameterMetaDataBuilder jndiUninstall = bootstrap.addUninstallWithParameters("unbind", FallbackBeanManagerJndiBinder.class.getSimpleName() );
        jndiUninstall.addParameterMetaData(DeploymentUnit.class.getName(), unit);

        bootstrapBmd = bootstrap.getBeanMetaData();
        unit.addAttachment(DeployersUtils.getBootstrapBeanAttachmentName(unit), bootstrapBmd );
        
    }
    

}
