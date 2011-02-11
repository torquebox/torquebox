/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

package org.torquebox.services.deployers;

import java.util.Map;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.services.ServiceMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: services.yml, PoolMetaData
 *   Out: BeanMetaData, PoolMetaData
 * </pre>
 * 
 * Creates BeanMetaData instances from services.yml
 */
public class ServicesYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {


    public ServicesYamlParsingDeployer() {
        setSectionName( "services" );
        addOutput( ServiceMetaData.class );
    }

    @SuppressWarnings("unchecked")
    public void parse(VFSDeploymentUnit unit, Object dataObj) throws Exception {
        Map<String, Map<String, Object>> results = (Map<String, Map<String, Object>>) dataObj;
        if (results != null) {
            for (String service : results.keySet()) {
                Map<String, Object> params = results.get( service );
                ServiceMetaData serviceMetaData = new ServiceMetaData();
                boolean requiresSingleton = requiresSingleton(  params );
                serviceMetaData.setRequiresSingleton( requiresSingleton );
                serviceMetaData.setClassName( service  );
                serviceMetaData.setParameters( params );
                AttachmentUtils.multipleAttach( unit, serviceMetaData, serviceMetaData.getClassName() );
            }
        }
    }

    protected boolean requiresSingleton(Map<String, Object> params) {
        Boolean singleton = params == null ? null : (Boolean) params.remove( "singleton" );
        return singleton != null && singleton.booleanValue();
    }
}
