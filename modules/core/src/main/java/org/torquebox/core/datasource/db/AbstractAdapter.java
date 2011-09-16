package org.torquebox.core.datasource.db;

import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.validator.ValidateException;
import org.torquebox.core.datasource.DataSourceMetaData;
import org.torquebox.core.datasource.DatabaseMetaData;


public abstract class AbstractAdapter implements Adapter {

    public AbstractAdapter(String name, String driverClassName, String dataSourceClassName) {
        this.name = name;
        this.driverClassName = driverClassName;
        this.dataSourceClassName = dataSourceClassName;
    }
    
    @Override
    public String getName() {
        return this.name;
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

    private String name;
    private String driverClassName;
    private String dataSourceClassName;
}
