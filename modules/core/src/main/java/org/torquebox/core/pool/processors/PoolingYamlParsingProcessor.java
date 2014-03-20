/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.pool.processors;

import java.util.Map;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.processors.AbstractSplitYamlParsingProcessor;
import org.torquebox.core.runtime.PoolMetaData;

/**
 * 
 * Parsing deployer for {@code pooling.yml}.
 * 
 * <p>
 * This deployer looks for metadata files named exactly {@code pooling.yml},
 * which is expected to be a YAML file describing the configuration of various
 * Ruby runtime interpreter pools.
 * </p>
 * 
 * <p>
 * The top-level of the YAML file should be a hash, with the pool identifier as
 * the key. The value of each map may be the strings {@code global} or
 * {@code shared}, or another hash specifying {@code min} and {@code max} values
 * for the pool size.
 * </p>
 * 
 * <pre>
 *   pool_one: global
 *   pool_two: shared
 *   pool_three:
 *     min: 5
 *     max: 25
 * </pre>
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 * @see PoolMetaData
 */
public class PoolingYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    /**
     * Construct.
     */
    public PoolingYamlParsingProcessor() {
        setSectionName( "pooling" );
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        if (DeploymentUtils.isUnitRootless( phaseContext.getDeploymentUnit() )) {
            return;
        }
        super.deploy( phaseContext );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(DeploymentUnit unit, Object dataObj) throws Exception {
        Map<String, Object> pooling = (Map<String, Object>) dataObj;

        if (pooling != null) {
            for (String name : pooling.keySet()) {

                Object pool = pooling.get( name );

                PoolMetaData poolMetaData = new PoolMetaData( name );
                if (name.equals( "web" )) {
                    // Web runtimes default to eager
                    poolMetaData.setDeferUntilRequested( false );
                }

                if (pool instanceof Map) {
                    Map<String, Object> poolMap = (Map<String, Object>) pool;

                    if (poolMap.get( "type" ) != null) {
                        String type = poolMap.get( "type" ).toString();
                        if (type.equals( "shared" )) {
                            poolMetaData.setShared();
                        } else if (type.equals( "global" )) {
                            poolMetaData.setGlobal();
                        }
                    }

                    if (poolMap.get( "min" ) != null) {
                        poolMetaData.setMinimumSize( ((Number) poolMap.get( "min" )).intValue() );
                    }

                    if (poolMap.get( "max" ) != null) {
                        poolMetaData.setMaximumSize( ((Number) poolMap.get( "max" )).intValue() );
                    }

                    if (poolMap.get( "lazy" ) != null) {
                        poolMetaData.setDeferUntilRequested( (Boolean) poolMap.get( "lazy" ) );
                    }
                } else if (pool instanceof String) {
                    if (pool.toString().equals( "shared" )) {
                        poolMetaData.setShared();
                    } else if (pool.toString().equals( "global" )) {
                        poolMetaData.setGlobal();
                    }
                }
                unit.addToAttachmentList( PoolMetaData.ATTACHMENTS_KEY, poolMetaData );
            }
        }

    }

}
