package org.torquebox.core.datasource.db;

import java.util.HashMap;
import java.util.Map;

import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.ds.DsSecurityImpl;
import org.torquebox.core.datasource.DatabaseMetaData;

public class H2Adapter extends AbstractAdapter {

    public H2Adapter() {
        super( "h2", "org.h2.Driver", "org.h2.jdbcx.JdbcDataSource" );
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
        return new DsSecurityImpl( (String) config.get( "user" ), (String) config.get( "password" ), null, null );
    }

}
