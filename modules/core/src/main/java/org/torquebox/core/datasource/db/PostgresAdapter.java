package org.torquebox.core.datasource.db;

import java.util.HashMap;
import java.util.Map;

import org.torquebox.core.datasource.DatabaseMetaData;

public class PostgresAdapter extends AbstractAdapter {

    public PostgresAdapter() {
        super( "postgres", "org.postgresql.Driver", "org.postgresql.xa.PGXADataSource" );
    }
    
    @Override
    public String[] getNames() {
        return new String[] {
                "postgresql",
                "jdbcpostgresql",
        };
    }


    @Override
    public Map<String, String> getPropertiesFor(DatabaseMetaData dbMeta) {
        Map<String, Object> config = dbMeta.getConfiguration();
        Map<String, String> properties = new HashMap<String, String>();

        properties.put( "ServerName"   , null==config.get("host") ? "localhost" : ""+config.get("host") );
        properties.put( "PortNumber"   , null==config.get("port") ? "5432" : ""+config.get("port") );
        properties.put( "DatabaseName" , ""+config.get("database") );
        properties.put( "User"         , ""+config.get("username") );
        properties.put( "Password"     , ""+config.get("password") );

        return properties;
    }

}
