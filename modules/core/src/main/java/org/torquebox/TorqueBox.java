package org.torquebox;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class TorqueBox implements TorqueBoxMBean, Service<TorqueBox> {

    public TorqueBox(String version, String revision, String buildNumber, String buildUser) {
        this.version = version;
        this.revision = revision;
        this.buildNumber = buildNumber;
        this.buildUser = buildUser;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public String getRevision() {
        return this.revision;
    }
    
    public String getBuildNumber() {
        return this.buildNumber;
    }
    
    public String getBuildUser() {
        return this.buildUser;
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
    
    private String version;
    private String revision;
    private String buildNumber;
    private String buildUser;
}
