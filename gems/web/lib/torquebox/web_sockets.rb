require 'torquebox/service_registry'

module TorqueBox
  module WebSockets

    extend TorqueBox::Injectors

    def self.lookup(name)
      unit = inject( 'deployment-unit' )
      puts "unit #{unit}"
      helper = org.torquebox.web.websockets.WebSocketsServices::INSTANCE
      puts "helper #{helper}"
      service_name = helper.getUrlRegistryName( unit )
      url_registry = TorqueBox::ServiceRegistry[ service_name ]
      url_registry.lookupURL( name )
    end 

  end
end
