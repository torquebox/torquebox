require 'org.torquebox.torquebox-container-foundation'
require 'torquebox/container/foundation'

module TorqueBox
  module Container
    class MessagingEnabler

      def initialize(&block)
        instance_eval( &block ) if block
      end

      def fundamental_deployment_paths()
        [ File.join( File.dirname(__FILE__), 'messaging-jboss-beans.xml' ) ]
      end

      def before_start(container)
        Java::java.lang::System.setProperty( "torquebox.hornetq.configuration.url", 
                                             "file://" + File.join( File.dirname(__FILE__), 'hornetq-configuration.xml' ) )
      end
    

    end
  end
end
