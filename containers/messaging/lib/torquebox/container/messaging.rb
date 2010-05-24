require 'org.torquebox.torquebox-container-foundation'
require 'torquebox/container/foundation'

module TorqueBox
  module Container
    class Messaging < Foundation

      def initialize()
        super
      end

      def start
        super
        Java::java.lang.System.setProperty( "torquebox.hornetq.configuration.url", "file://" + File.join( File.dirname(__FILE__), 'hornetq-configuration.xml' ) )
        beans_xml = File.join( File.dirname(__FILE__), 'messaging-jboss-beans.xml' )
        puts "beans_xml=#{beans_xml}"
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
