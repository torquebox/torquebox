package org.torquebox.core.datasource.db;

import java.util.HashMap;
import java.util.Map;

import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.ds.DsSecurityImpl;
import org.torquebox.core.datasource.DatabaseMetaData;

public class PostgresAdapter extends AbstractAdapter {

    public PostgresAdapter() {
        super( "postgresql", "org.postgresql.Driver", "org.postgresql.xa.PGXADataSource" );
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
