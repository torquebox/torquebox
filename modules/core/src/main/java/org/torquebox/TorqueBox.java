package org.torquebox;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.torquebox.core.util.BuildInfo;

public class TorqueBox implements TorqueBoxMBean, Service<TorqueBox> {

    public TorqueBox() throws IOException {
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
        log.info( formatOutput( "version", getVersion() ) );
        String buildNo = getBuildNumber();
        if (buildNo != null && !buildNo.trim().equals( "" )) {
            log.info( formatOutput( "build", getBuildNumber() ) );
        } else if (getVersion().contains( "SNAPSHOT" )) {
            log.info( formatOutput( "build", "development (" + getBuildUser() + ")" ) );
        } else {
            log.info( formatOutput( "build", "official" ) );
        }
        log.info( formatOutput( "revision", getRevision() ) );

        List<String> otherCompoments = this.buildInfo.getComponentNames();
        otherCompoments.remove( "TorqueBox" );
        log.info( "  featuring:" );
        for(String name: otherCompoments) {
            String version = this.buildInfo.get( name, "version" );
            if (version != null) {
                log.info( formatOutput( " " + name, version ) );
            }
        }

    }

    @Override
        public void stop(StopContext context) {

    }

    private String formatOutput(String label, String value) {

        StringBuffer output = new StringBuffer( "  " );
        output.append( label );
        int length = output.length();
        if (length < 20) {
            for(int i = 0; i < 20 - length; i++) {
                output.append(  '.' );
            }
        }

        output.append( ' ' );
        output.append( value );

        return output.toString();
    }

    private BuildInfo buildInfo;

}
