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

import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.ds.ValidationImpl;
import org.torquebox.core.datasource.DatabaseMetaData;

public class MySQLAdapter extends AbstractAdapter {

    public MySQLAdapter() {
        super( "mysql", "jdbc/mysql", "Jdbc::MySQL", "com.mysql.jdbc.Driver", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource" );
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

        properties.put( "ServerName", null == config.get( "host" ) ? "localhost" : "" + config.get( "host" ) );
        properties.put( "PortNumber", null == config.get( "port" ) ? "3306" : "" + config.get( "port" ) );
        properties.put( "DatabaseName", "" + config.get( "database" ) );
        properties.put( "User", "" + config.get( "username" ) );
        properties.put( "Password", "" + config.get( "password" ) );

        return properties;
    }

    @Override
    public Validation getValidationFor(DatabaseMetaData dbMeta) throws ValidateException {
        Boolean backgroundValidation = false;
        Long backgroundValidationMillis = 0L;
        Boolean useFastFail = false;
        Extension validConnectionChecker = new Extension( "org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker", null );
        String checkValidConnectionSql = null;
        Boolean validateOnMatch = false;
        Extension staleConnectionChecker = null;
        Extension exceptionSorter = new Extension( "org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter", null );

        return new ValidationImpl(
                backgroundValidation,
                backgroundValidationMillis,
                useFastFail,
                validConnectionChecker,
                checkValidConnectionSql,
                validateOnMatch,
                staleConnectionChecker,
                exceptionSorter );
    }

}
