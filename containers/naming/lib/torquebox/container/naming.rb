require 'org.torquebox.torquebox-container-foundation'
require 'torquebox/container/foundation'

module TorqueBox
  module Container
    class Naming < Foundation

      def initialize()
        super
        Java::java.lang.System.setProperty( 'java.naming.factory.initial', 'org.jnp.interfaces.NamingContextFactory' )
        Java::java.lang.System.setProperty( 'java.naming.factory.url.pkgs', 'org.jboss.naming:org.jnp.interfaces' )
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
