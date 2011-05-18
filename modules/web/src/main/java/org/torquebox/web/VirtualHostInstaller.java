package org.torquebox.web;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.services.path.AbstractPathService;
import org.jboss.as.web.WebServer;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.msc.service.ServiceName;
import org.torquebox.web.rack.RackApplicationMetaData;

public class VirtualHostInstaller implements DeploymentUnitProcessor {
    
    private static final String TEMP_DIR = "jboss.server.temp.dir";

    private static final String[] EMPTY_STRING_ARRAY = new String[]{};
    
    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RackApplicationMetaData rackMetaData = unit.getAttachment(RackApplicationMetaData.ATTACHMENT_KEY);
        
        if ( rackMetaData == null ) {
            return;
        }
        
        List<String> hosts = new ArrayList<String>();
        hosts.addAll( rackMetaData.getHosts() );
        
        if ( hosts.isEmpty() ) {
            return;
        }
        
        String name = hosts.remove( 0 );
        
        ServiceName serviceName = WebSubsystemServices.JBOSS_WEB_HOST.append(name);
        
        if ( phaseContext.getServiceRegistry().getService( serviceName ) != null ) {
            return;
        }
        
        String[] aliases = hosts.toArray( EMPTY_STRING_ARRAY );
        
        VirtualHostService service = new VirtualHostService( name, aliases );
        
        phaseContext.getServiceTarget().addService( serviceName, service )
           .addDependency(AbstractPathService.pathNameOf(TEMP_DIR), String.class, service.getTempPathInjector())
           .addDependency(WebSubsystemServices.JBOSS_WEB, WebServer.class, service.getWebServer())
           .install();
    }
    

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub
        
    }

}
