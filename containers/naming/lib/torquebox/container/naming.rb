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
        puts "beans_xml=#{beans_xml}"
        @core_deployers = deploy( beans_xml )
        process_deployments(true)
        puts "naming is #{self['Naming']}"
      end

      def stop
        jndi = self['JNDIServer']
        puts "naming is #{jndi}"
        executor = jndi.getLookupExector()
        puts "executor is #{executor}"
        executor.shutdown
        undeploy( @core_deployers )
        process_deployments(true)
        super
      end

    end
  end
end
