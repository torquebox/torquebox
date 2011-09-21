package org.torquebox.core.datasource.db;

import java.util.HashMap;
import java.util.Map;

import org.torquebox.core.datasource.DatabaseMetaData;

public class MySQLAdapter extends AbstractAdapter {

    public MySQLAdapter() {
        super( "mysql", "jdbc/mysql", "com.mysql.jdbc.Driver", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource" );
    }
    
    @Override
    public String[] getNames() {
        return new String[] {
                "mysql",
                "mysql2",
                "jdbcmysql",
                "jdbcmysql2",
        };
    }


    @Override
    public Map<String, String> getPropertiesFor(DatabaseMetaData dbMeta) {
        Map<String, Object> config = dbMeta.getConfiguration();
        Map<String, String> properties = new HashMap<String, String>();

        properties.put( "ServerName"   , null==config.get("host") ? "localhost" : ""+config.get("host") );
        properties.put( "PortNumber"   , null==config.get("port") ? "3306" : ""+config.get("port") );
        properties.put( "DatabaseName" , ""+config.get("database") );
        properties.put( "User"         , ""+config.get("username") );
        properties.put( "Password"     , ""+config.get("password") );

        return properties;
    }

}
