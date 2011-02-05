package org.torquebox.mc.vdf;

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

public abstract class AbstractMultipleMetaDataDeployer<T> extends AbstractDeployer {

    private Class<T> metaDataClass;

    public AbstractMultipleMetaDataDeployer(Class<T> metaDataClass) {
        addInput( metaDataClass );
        this.metaDataClass = metaDataClass;
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        Set<? extends T> metaData = unit.getAllMetaData( this.metaDataClass );

        for (T each : metaData) {
            deploy( unit, each );
        }

    }

    protected abstract void deploy(DeploymentUnit unit, T metaData) throws DeploymentException;

}
