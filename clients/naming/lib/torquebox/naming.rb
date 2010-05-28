
module TorqueBox
  class Naming

    FACTORY = 'org.jnp.interfaces.NamingContextFactory'
    FACTORY_URL_PKGS = 'org.jboss.naming:org.jnp.interfaces'

    class Configuration
      attr_accessor :host
      attr_accessor :port

      def initialize(host='localhost', port=1099)
        @host = host
        @port = port
      end

    end

    def self.configure(&block)
      config = Configuration.new
      if ( block ) 
        block.call( config ) 
      end
      url = "jnp://#{config.host}:#{config.port}/"
      java.lang::System.setProperty( 'java.naming.provider.url', url )
      java.lang::System.setProperty( 'java.naming.factory.initial', FACTORY )
      java.lang::System.setProperty( 'java.naming.factory.url.pkgs', FACTORY_URL_PKGS )
    end

    def self.[](name)
      context = javax.naming::InitialContext.new
      context.lookup( name )
    end

    def self.[]=(name, value)
      context = javax.naming::InitialContext.new
      context.rebind( name, value )
    end

  end
end
