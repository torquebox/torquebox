package org.torquebox;

import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.torquebox.core.util.BuildInfo;

public class TorqueBox implements TorqueBoxMBean, Service<TorqueBox> {

	public TorqueBox() {
		this.buildInfo = new BuildInfo();	
    }
    
    public String getVersion() {
    	return this.buildInfo.get( "TorqueBox", "version" );
    }
    
    public String getRevision() {
    	return this.buildInfo.get( "TorqueBox", "build.revision" );
    }
    
    public String getBuildNumber() {
    	return this.buildInfo.get( "TorqueBox", "build.number" );
        }
    
    public String getBuildUser() {
    	return this.buildInfo.get( "TorqueBox", "build.user" );
    }

    public List<String> getComponentNames() {
    	return this.buildInfo.getComponentNames();
    }
    
    public Map<String, String> getComponentBuildInfo(String componentName) {
    	return this.buildInfo.getComponentInfo( componentName );
    }
    
    @Override
    public TorqueBox getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
    }
    
    public void dump(Logger log) {
    	log.info( "Welcome to TorqueBox AS - http://torquebox.org/" );
        log.info( "  version...... " + getVersion() );
        String buildNo = getBuildNumber();
        if (buildNo != null && !buildNo.trim().equals( "" )) {
            log.info( "  build........ " + getBuildNumber() );
        } else if (getVersion().contains( "SNAPSHOT" )) {
            log.info( "  build........ development (" + getBuildUser() + ")" );
        } else {
            log.info( "  build........ official" );
        }
        log.info( "  revision..... " + getRevision() );
    }

    @Override
    public void stop(StopContext context) {
        
    }
    
    private BuildInfo buildInfo;
    
}
