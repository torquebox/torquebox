
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
    
    def self.context(host=nil, port=nil)
      if ( ! ( host.nil? || port.nil? ) ) 
        props = java.util.Hashtable.new( {
          'java.naming.provider.url'=>"jnp://#{host}:#{port}/",
          'java.naming.factory.initial'=>FACTORY,
          'java.naming.factory.url.pkgs'=>'org.jboss.naming:org.jnp.interfaces'
        } )
        puts "grabbing context with #{props.to_a.inspect}"
        javax.naming::InitialContext.new(props)
      else
        puts "grabbing default context"
        javax.naming::InitialContext.new
      end
    end

    def self.connect(host, port, &block)
      puts "connect(#{host}, #{port}, ...)"
      puts "is block nil? #{block.nil?}"
      return context(host, port) if ( block.nil? )

      reconfigure_on_error do
        ctx = context(host, port)
        puts "ctx=#{ctx}"
        begin
          block.call( ctx )
        ensure
          ctx.close
        end
      end
    end

    def self.reconfigure_on_error( max_retries = 1 )
      attempts = 0
      begin
        attempts += 1
        yield
      rescue=>e
        puts e
        je = e.cause
        puts je.cause
        if attempts > max_retries
          raise
        else
          puts "about to reconfigure and retry"
          configure
          retry
        end
      end
    end

  end
end
