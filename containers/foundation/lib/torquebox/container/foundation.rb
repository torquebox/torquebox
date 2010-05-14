
module TorqueBox
  module Container
    class Foundation

      def initialize()
        @server = Java::org.jboss.bootstrap.api.mc.server::MCServerFactory.createServer();
  
        descriptors = @server.configuration.bootstrap_descriptors
        descriptors << Java::org.jboss.reloaded.api::ReloadedDescriptors.class_loading_descriptor
        descriptors << Java::org.jboss.reloaded.api::ReloadedDescriptors.vdf_descriptor
        @deployers = Deployers.new( self )
      end
  
      def start
        @server.start
      end
  
      def stop
        @server.stop
      end
  
      def deployers
        @deployers
      end
  
      def add_deployer(deployer)
        deployer_name = deployer.class.simple_name
        controller = @server.kernel.controller
        bmdb = Java::org.jboss.beans.metadata.spi.builder::BeanMetaDataBuilder.createBuilder(deployer_name, deployer.class.name)
        controller.install(bmdb.bean_meta_data, deployer);
      end
  
  
      class Deployers
        def initialize(container)
          @container = container
        end
  
        def <<(deployer)
          @container.add_deployer( deployer )
        end
      end

    end
  end
end
