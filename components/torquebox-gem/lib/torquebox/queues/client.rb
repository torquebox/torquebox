
module TorqueBox
  module Queues
    class Client
      def initialize(destination, naming_host=localhost, naming_port=1099)

        # let's try to find the proper JBoss bind address if naming_host is localhost
        naming_host = Java::java.lang.System.getProperty( "jboss.bind.address" ) if naming_host.eql?(RubyClient::DEFAULT_NAMING_HOST)

        @client = org.torquebox.ruby.enterprise.queues.RubyTaskQueueClient.new( naming_host, naming_port )
        @client.set_destination( destination )
      end

      def enqueue(task_name, payload=nil)
        @client.enqueue( task_name, payload ) 
      end

    end
  end
end
