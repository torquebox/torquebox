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

import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.validator.ValidateException;
import org.torquebox.core.datasource.DatabaseMetaData;


public abstract class AbstractAdapter implements Adapter {

    public AbstractAdapter(String id, String requirePath, String rubyDriverClassName, String driverClassName, String dataSourceClassName) {
        this.id = id;
        this.requirePath = requirePath;
        this.rubyDriverClassName = rubyDriverClassName;
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
    public String getRubyDriverClassName() {
        return this.rubyDriverClassName;
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
    
    @Override
    public Validation getValidationFor(DatabaseMetaData dbMeta) throws ValidateException {
        return null;
    }


    private String id;
    private String requirePath;
    private String rubyDriverClassName;
    private String driverClassName;
    private String dataSourceClassName;
}
