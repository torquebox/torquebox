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

package org.torquebox.interp.deployers;

import java.util.Map;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;

/**
 * <pre>
 * Stage: PARSE
 *    In: pooling.yml
 *   Out: PoolMetaData
 * </pre>
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
public class PoolingYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {

    /**
     * Construct.
     */
    public PoolingYamlParsingDeployer() {
        setSectionName( "pooling" );
        addOutput( PoolMetaData.class );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(VFSDeploymentUnit unit, Object dataObj) throws Exception {
        Map<String, Object> pooling = (Map<String, Object>) dataObj;

        if (pooling != null) {
            for (String name : pooling.keySet()) {

                Object pool = pooling.get( name );

                PoolMetaData poolMetaData = new PoolMetaData( name );

                if (pool instanceof Map) {
                    Map<String, Object> poolMap = (Map<String, Object>) pool;

                    if (poolMap.get( "min" ) != null) {
                        poolMetaData.setMinimumSize( ((Number) poolMap.get( "min" )).intValue() );
                    }

                    if (poolMap.get( "max" ) != null) {
                        poolMetaData.setMaximumSize( ((Number) poolMap.get( "max" )).intValue() );
                    }
                } else if (pool instanceof String) {
                    if (pool.toString().equals( "shared" )) {
                        poolMetaData.setShared();
                    } else if (pool.toString().equals( "global" )) {
                        poolMetaData.setGlobal();
                    }
                }
                log.info( "Configured Ruby runtime pool: " + poolMetaData );
                AttachmentUtils.multipleAttach( unit, poolMetaData, name );
            }
        }

    }

}
