package org.torquebox.core.datasource.db;

import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.validator.ValidateException;
import org.torquebox.core.datasource.DatabaseMetaData;


public abstract class AbstractAdapter implements Adapter {

    public AbstractAdapter(String id, String requirePath, String driverClassName, String dataSourceClassName) {
        this.id = id;
        this.requirePath = requirePath;
        this.driverClassName = driverClassName;
        this.dataSourceClassName = dataSourceClassName;
    }
    
    @Override
    public String getId() {
        return this.id;
    }
    
    @Override
    public String getRequirePath() {
        return this.requirePath;
    }

    @Override
    public String getDriverClassName() {
        return this.driverClassName;
    }

    @Override
    public String getDataSourceClassName() {
        return this.dataSourceClassName;
    }
    
    @Override
    public DsSecurity getSecurityFor(DatabaseMetaData dsMeta) throws ValidateException {
        return null;
    }

    private String id;
    private String requirePath;
    private String driverClassName;
    private String dataSourceClassName;
}
