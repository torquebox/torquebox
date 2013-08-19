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

import org.torquebox.core.datasource.DatabaseMetaData;

public class OracleAdapter extends AbstractAdapter {

    public OracleAdapter() {
        super( "oracle", "ojdbc6.jar", null, "oracle.jdbc.OracleDriver", "oracle.jdbc.xa.client.OracleXADataSource" );
    }
    
    @Override
    public String[] getNames() {
        return new String[] {
            "oracle",
        };
    }

    @Override
    public Map<String, String> getPropertiesFor(DatabaseMetaData dbMeta) {
        Map<String, Object> config = dbMeta.getConfiguration();
        Map<String, String> properties = new HashMap<String, String>();
        
        String url = (String) config.get( "url" );
        if (url == null) {
            String host = config.get( "host" ) == null ? "localhost" : (String) config.get( "host" );
            int port = config.get( "port" ) == null ? 1521 : (Integer) config.get( "port" );
            String database = (String) config.get( "database" );
            url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + database;
        }

        properties.put( "URL"     , url );
        properties.put( "User"    , ""+config.get("username") );
        properties.put( "Password", ""+config.get("password") );

        return properties;
    }

}
