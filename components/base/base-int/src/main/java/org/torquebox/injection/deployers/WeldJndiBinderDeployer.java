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

public class WeldJndiBinderDeployer extends AbstractDeployer {
    
    public WeldJndiBinderDeployer() {
        setStage( DeploymentStages.PRE_REAL );
        setInput( RubyApplicationMetaData.class );
        addInput( BeanMetaData.class );
        addOutput( BeanMetaData.class );
        setTopLevelOnly( true );
        setRelativeOrder( 10000 );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        System.err.println( "FIX CANDIDATE: "+ unit );
        String bootstrapAttachmentName = DeployersUtils.getBootstrapBeanAttachmentName(unit);
        BeanMetaData bootstrapBmd = unit.getAttachment( bootstrapAttachmentName, BeanMetaData.class );
        
        if ( bootstrapBmd == null ) {
            Set<? extends BeanMetaData> all = unit.getAllMetaData( BeanMetaData.class );
            
            for ( BeanMetaData each : all ) {
                System.err.println( "FIX DUMP: " + each );
            }
            return;
        }
        
        System.err.println( "==> FIXING UP BMD: " + bootstrapBmd );
        BeanMetaDataBuilder bootstrap = BeanMetaDataBuilder.createBuilder( bootstrapBmd );
        
        ParameterMetaDataBuilder jndiInstall = bootstrap.addInstallWithParameters("bind", "TorqueBoxWeldJndiBinder", ControllerState.INSTALLED, ControllerState.START);
        jndiInstall.addParameterMetaData(DeploymentUnit.class.getName(), unit);

        ParameterMetaDataBuilder jndiUninstall = bootstrap.addUninstallWithParameters("unbind", "TorqueBoxWeldJndiBinder");
        jndiUninstall.addParameterMetaData(DeploymentUnit.class.getName(), unit);

        bootstrapBmd = bootstrap.getBeanMetaData();
        System.err.println( "==> FIXED UP BMD: " + bootstrapBmd );
        unit.addAttachment(DeployersUtils.getBootstrapBeanAttachmentName(unit), bootstrapBmd );
        
    }
    

}
