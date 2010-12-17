require 'torquebox/container/foundation'

module TorqueBox
  module Messaging
    class MessageProcessorHost

      def initialize(&block)
        instance_eval( &block ) if block
      end

      def fundamental_deployment_paths()
        [ File.join( File.dirname(__FILE__), 'message-processor-host-jboss-beans.xml' ) ]
      end

=begin
      def before_start(container)
        org.hornetq.core.logging::Logger.setDelegateFactory( org.hornetq.integration.logging::Log4jLogDelegateFactory.new )
        Java::java.lang::System.setProperty( "torquebox.hornetq.configuration.url", 
                                             "file://" + File.join( File.dirname(__FILE__), 'hornetq-configuration.xml' ) )
      end
=end
    

    end
  end
end
