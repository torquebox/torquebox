
require 'torquebox/naming/ext/javax_naming_context'
module TorqueBox
  module Naming

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

    def self.configure_local
      java.lang::System.clearProperty( 'java.naming.provider.url' )
      java.lang::System.setProperty( 'java.naming.factory.initial', FACTORY )
    end

    def self.[](name)
      context = javax.naming::InitialContext.new
      begin
        return context[ name ]
      ensure
        context.close
      end
    end

    def self.[]=(name, value)
      context = javax.naming::InitialContext.new
      begin
        #context.rebind( name, value )
        context[name] = value
      ensure
        context.close
      end
    end

    def self.connect(host, port, &block)
      context = nil

      if ( ! ( host.nil? || port.nil? ) ) 
        props = java.util.Hashtable.new( {
          'java.naming.provider.url'=>"jnp://#{host}:#{port}/",
          'java.naming.factory.initial'=>FACTORY,
        } )
        context = javax.naming::InitialContext.new(props)
      else
        context = javax.naming::InitialContext.new
      end

      return context if ( block.nil? )

      attempts = 0
      begin
        attempts += 1
        block.call( context )
      rescue
        if attempts > 1
          raise
        else
          configure
          context = javax.naming::InitialContext.new
          retry
        end
      ensure
        context.close
      end
    end

  end
end
