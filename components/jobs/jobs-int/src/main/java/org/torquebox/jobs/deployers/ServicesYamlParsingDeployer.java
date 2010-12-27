/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.torquebox.jobs.deployers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractParsingDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.common.util.StringUtils;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.interp.core.InstantiatingRubyComponentResolver;
import org.torquebox.interp.core.RubyComponentResolver;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.jobs.core.RubyServiceProxy;
import org.yaml.snakeyaml.Yaml;


/**
 * <pre>
 * Stage: PARSE
 *    In: services.yml, PoolMetaData
 *   Out: BeanMetaData, PoolMetaData
 * </pre>
 *
 * Creates BeanMetaData instances from services.yml
 */
public class ServicesYamlParsingDeployer extends AbstractParsingDeployer {

    public static final String POOL_NAME = "services";

	public ServicesYamlParsingDeployer() {
        addInput(PoolMetaData.class);
		addOutput(BeanMetaData.class);
        addOutput(PoolMetaData.class);
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		if (unit instanceof VFSDeploymentUnit) {
			deploy((VFSDeploymentUnit) unit);
		}
	}

	protected void deploy(VFSDeploymentUnit unit) throws DeploymentException {
		VirtualFile metaData = unit.getMetaDataFile("services.yml");
		if (metaData != null) {
            try {
                int count = parse(unit, metaData);
                createRuntimePool(unit, count);
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
		}
	}

	protected int parse(VFSDeploymentUnit unit, VirtualFile file) throws IOException {
        int result = 0;
		InputStream in = file.openStream();
		try {
			Yaml yaml = new Yaml();
			Map<String, Map<String, String>> results = (Map<String, Map<String, String>>) yaml.load(in);
			if (results != null) {
                result = results.size();
				for (String service : results.keySet()) {
					Map<String, String> params = results.get(service);
                    createServiceProxyBean( unit, service, params );
				}
			}
		} finally {
            in.close();
		}
        return result;
	}

    protected void createServiceProxyBean(DeploymentUnit unit, String service, Map params) {
        String beanName = AttachmentUtils.beanName( unit, RubyServiceProxy.class, service );
        BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder( beanName, RubyServiceProxy.class.getName() );

        ValueMetaData runtimePoolInject = builder.createInject(AttachmentUtils.beanName(unit, RubyRuntimePool.class, POOL_NAME) );
        builder.addPropertyMetaData("rubyRuntimePool", runtimePoolInject);
        builder.addPropertyMetaData("rubyComponentResolver", createComponentResolver( service, params ));

        AttachmentUtils.attach(unit, builder.getBeanMetaData());
    }

	protected RubyComponentResolver createComponentResolver(String service, Map params) {
		InstantiatingRubyComponentResolver result = new InstantiatingRubyComponentResolver();
		result.setRubyClassName( StringUtils.camelize( service ) );
		result.setRubyRequirePath( StringUtils.underscore( service ) );
        result.setInitializeParams( params );
		result.setComponentName("service." + service);
		return result;
	}

    protected PoolMetaData createRuntimePool(DeploymentUnit unit, int max) {
        PoolMetaData pool = AttachmentUtils.getAttachment( unit, POOL_NAME, PoolMetaData.class );;
        if ( pool == null && max > 0 ) {
            pool = new PoolMetaData(POOL_NAME, 1, max);
            log.info("Configured Ruby runtime pool for services: " + pool);
            AttachmentUtils.multipleAttach(unit, pool, POOL_NAME);
        }
        return pool;
    }
}