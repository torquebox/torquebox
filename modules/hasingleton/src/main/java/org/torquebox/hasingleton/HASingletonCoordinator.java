package org.torquebox.hasingleton;

import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class HASingletonCoordinator extends ReceiverAdapter {


    public HASingletonCoordinator(ServiceController<Void> haSingletonController, Channel channel, String partitionName) {
        this.haSingletonController = haSingletonController;
        this.channel = channel;
        this.channel.setReceiver(  this );
        this.partitionName = partitionName;
    }
    
    public void start() throws Exception {
        this.channel.connect( this.partitionName );
    }
    
    public void stop() throws Exception {
        this.channel.disconnect();
    }
    
    @Override
    public void viewAccepted(View view) {
        if ( shouldBeMaster( view ) ) {
            log.info( "Becoming HASingleton master." );
            haSingletonController.setMode( Mode.ACTIVE );
        } else {
            haSingletonController.setMode( Mode.NEVER );
        }
    }
    
    protected boolean shouldBeMaster(View view) {
        if ( view.getMembers().isEmpty() ) {
            return false;
        }
        
        Address coordinator = view.getMembers().get( 0 );
        
        return channel.getAddress().equals( coordinator );
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.hasingleton" );
    
    private ServiceController<Void> haSingletonController;
    private Channel channel;
    private String partitionName;

}
