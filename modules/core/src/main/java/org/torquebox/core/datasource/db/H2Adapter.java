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

package org.torquebox.core.datasource.db;

import java.util.HashMap;
import java.util.Map;

import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.ds.DsSecurityImpl;
import org.torquebox.core.datasource.DatabaseMetaData;

public class H2Adapter extends AbstractAdapter {

    public H2Adapter() {
        super( "h2", "jdbc/h2", "Jdbc::H2", "org.h2.Driver", "org.h2.jdbcx.JdbcDataSource" );
    }

    @Override
    public String[] getNames() {
        return new String[] {
                "h2",
                "jdbch2",
        };
    }

    @Override
    public Map<String, String> getPropertiesFor(DatabaseMetaData dbMeta) {
        Map<String, Object> config = dbMeta.getConfiguration();

        Map<String, String> properties = new HashMap<String, String>();

        String configUrl = (String) config.get( "url" );
        if (configUrl == null) {
            properties.put( "URL", "jdbc:h2:" + config.get( "database" ) );
        } else {
            properties.put( "URL", configUrl );
        }
        return properties;
    }

    public DsSecurity getSecurityFor(DatabaseMetaData dbMeta) throws ValidateException {
        Map<String, Object> config = dbMeta.getConfiguration();
        return new DsSecurityImpl( (String) config.get( "username" ), (String) config.get( "password" ), null, null );
    }

}
