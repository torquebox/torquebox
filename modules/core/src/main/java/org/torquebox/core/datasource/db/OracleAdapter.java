package org.torquebox.core.datasource.db;

import java.util.HashMap;
import java.util.Map;

import org.torquebox.core.datasource.DatabaseMetaData;

public class OracleAdapter extends AbstractAdapter {

    public OracleAdapter() {
        super( "oracle", "jdbc/oracle", "oracle.jdbc.driver.OracleDriver", "oracle.jdbc.xa.client.OracleXADataSource" );
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

        properties.put( "URL"      , ""+config.get("url"));
        properties.put( "User"     , ""+config.get("username") );
        properties.put( "Password" , ""+config.get("password") );

        return properties;
    }

}
