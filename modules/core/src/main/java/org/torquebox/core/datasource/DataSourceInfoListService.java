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

package org.torquebox.core.datasource;

import java.util.logging.Level;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.InjectionException;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.torquebox.core.datasource.DataSourceInfoList.Info;

public class DataSourceInfoListService implements Service<DataSourceInfoList> {
    
    public DataSourceInfoListService(Level restoreLevel) {
        this.restoreLevel = restoreLevel;
    }

    @Override
    public DataSourceInfoList getValue() throws IllegalStateException, IllegalArgumentException {
        return this.list;
    }

    @Override
    public void start(StartContext context) throws StartException {
        org.jboss.logmanager.Logger.getLogger( "com.arjuna.ats" ).setLevel( this.restoreLevel );
    }

    @Override
    public void stop(StopContext context) {

    }

    public Injector<Info> getInfoInjector() {
        return new Injector<Info>() {
            public void inject(Info value) throws InjectionException {
                if (value != Info.DISABLED) {
                    list.addConfiguration( value );
                }
            }

            public void uninject() {
            }
        };
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.core.datasource.xa" );

    private DataSourceInfoList list = new DataSourceInfoList();

    private Level restoreLevel;
}
