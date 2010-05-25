require 'org.torquebox.torquebox-container-foundation'
require 'torquebox/container/foundation'

module TorqueBox
  module Container
    class Naming < Foundation

      def initialize()
        super
        #Java::java.lang::System.setProperty( 'java.naming.provider.url',     'jnp://localhost:1099/' )
        Java::java.lang::System.setProperty( 'java.naming.factory.initial',  'org.jnp.interfaces.NamingContextFactory' )
        Java::java.lang::System.setProperty( 'java.naming.factory.url.pkgs', 'org.jboss.naming:org.jnp.interfaces' )
        Java::java.lang::System.setProperty( 'jnp.host', '10.42.42.11' )
        #Java::java.lang::System.setProperty( 'jnp.host', '10.11.8.117' )
      end

      def start
        super
        beans_xml = File.join( File.dirname(__FILE__), 'naming-jboss-beans.xml' )
        @core_deployers = deploy( beans_xml )
        process_deployments(true)
      end

      def stop
        undeploy( @core_deployers )
        process_deployments(true)
        super
      end

    end
  end
end
