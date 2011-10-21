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
