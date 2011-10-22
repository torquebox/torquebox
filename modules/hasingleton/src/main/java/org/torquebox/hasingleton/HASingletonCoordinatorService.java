package org.torquebox.hasingleton;

import org.jboss.as.clustering.jgroups.ChannelFactory;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jgroups.Channel;

public class HASingletonCoordinatorService implements Service<HASingletonCoordinator> {

    public HASingletonCoordinatorService(ServiceController<Void> haSingletonController, String partitionName) {
        this.haSingletonController = haSingletonController;
        this.partitionName = partitionName;
    }

    @Override
    public HASingletonCoordinator getValue() throws IllegalStateException, IllegalArgumentException {
        return this.coordinator;
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            ChannelFactory factory = this.channelFactoryInjector.getValue();
            this.channel = factory.createChannel( this.partitionName );
            this.coordinator = new HASingletonCoordinator( this.haSingletonController, channel, this.partitionName );
            this.coordinator.start();
        } catch (Exception e) {
            throw new StartException( e );
        }
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.coordinator.stop();
            this.coordinator = null;
            this.channel.close();
        } catch (Exception e) {
            log.error( "Unable to stop HA partition", e );
        }
    }

    public Injector<ChannelFactory> getChannelFactoryInjector() {
        return this.channelFactoryInjector;
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.topology" );

    private ServiceController<Void> haSingletonController;
    private String partitionName;

    private InjectedValue<ChannelFactory> channelFactoryInjector = new InjectedValue<ChannelFactory>();;
    
    private Channel channel;
    private HASingletonCoordinator coordinator;


}
