require 'org.torquebox.torquebox-container-foundation'
require 'torquebox/container/foundation'

module TorqueBox
  module Naming
    class NamingService

      def fundamental_deployment_paths
        paths = [ File.join( File.dirname(__FILE__), 'naming-local-jboss-beans.xml' ) ]
        if ( @export )
          paths << File.join( File.dirname(__FILE__), 'naming-rmi-jboss-beans.xml' )
        end
        paths
      end


      attr_accessor :port
      attr_accessor :host

      attr_accessor :rmi_port
      attr_accessor :rmi_host

      attr_accessor :export

      def initialize(&block)
        @host = 'localhost'
        @port = 1099

        @rmi_host = 'localhost'
        @rmi_port = 1098

        @export = true

        instance_eval(&block) if block
      end

      def before_start(container)
        Java::java.lang::System.setProperty( 'java.naming.factory.initial',  'org.jnp.interfaces.NamingContextFactory' )
        Java::java.lang::System.setProperty( 'java.naming.factory.url.pkgs', 'org.jboss.naming:org.jnp.interfaces' )
        Java::java.lang::System.setProperty( 'jnp.host', self.host.to_s )
        Java::java.lang::System.setProperty( 'jnp.port', self.port.to_s )
        Java::java.lang::System.setProperty( 'jnp.rmiHost', self.rmi_host.to_s )
        Java::java.lang::System.setProperty( 'jnp.rmiPort', self.rmi_port.to_s )
      end

      #def after_start(container)
      #  naming = container['JNDIServer'].naming_instance
      #  puts "naming is #{naming.inspect}"
      #  org.jnp.interfaces::NamingContext.setLocal( naming.to_java )
      #end


    end
  end
end
