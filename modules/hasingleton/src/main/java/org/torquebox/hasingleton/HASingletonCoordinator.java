package org.torquebox.hasingleton;

import java.util.List;

import org.jboss.as.clustering.ClusterNode;
import org.jboss.as.clustering.CoreGroupCommunicationService;
import org.jboss.as.clustering.GroupMembershipListener;
import org.jboss.as.clustering.jgroups.ChannelFactory;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;

public class HASingletonCoordinator implements GroupMembershipListener {
    
    private CoreGroupCommunicationService service;
    public HASingletonCoordinator(ServiceController<Void> haSingletonController, ChannelFactory channelFactory, String partitionName) {
        this.haSingletonController = haSingletonController;
        this.channelFactory = channelFactory;
        //this.channel.setReceiver(  this );
        //this.channel.addChannelListener( this );
        this.partitionName = partitionName;
    }
    
    public void start() throws Exception {
        log.info( "Connect to " + this.partitionName );
        this.service = new CoreGroupCommunicationService();
        this.service.setAllowSynchronousMembershipNotifications( true );
        this.service.setChannelFactory( this.channelFactory );
        this.service.registerGroupMembershipListener( this );
        this.service.setScopeId( (short) 248 );
        this.service.setChannelStackName( "jgroups-udp" );
        this.service.setGroupName( this.partitionName );
        this.service.start();
    }
    
    public void stop() throws Exception {
        this.service.stop();
    }
    
    protected boolean shouldBeMaster(List<ClusterNode> members) {
        log.info( "inquire if we should be master" );
        if ( members.isEmpty() ) {
            return false;
        }
        
        ClusterNode coordinator = members.get( 0 );
        
        return this.service.getClusterNode().equals( coordinator );
    }
    
    @Override
    public void membershipChanged(List<ClusterNode> deadMembers, List<ClusterNode> newMembers, List<ClusterNode> allMembers) {
        if ( shouldBeMaster( allMembers ) ) {
            log.info( "Becoming HASingleton master." );
            haSingletonController.setMode( Mode.ACTIVE );
        } else {
            log.info( "Ensuring NOT HASingleton master." );
            haSingletonController.setMode( Mode.NEVER );
        }
    }

    @Override
    public void membershipChangedDuringMerge(List<ClusterNode> deadMembers, List<ClusterNode> newMembers, List<ClusterNode> allMembers, List<List<ClusterNode>> originatingGroups) {
        membershipChanged( deadMembers, newMembers, allMembers );
    }

    
    private static final Logger log = Logger.getLogger( "org.torquebox.hasingleton" );
    
    private ServiceController<Void> haSingletonController;
    private ChannelFactory channelFactory;
    private String partitionName;
}
