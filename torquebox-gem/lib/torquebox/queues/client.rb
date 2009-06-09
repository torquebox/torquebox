
module TorqueBox
  module Queues
    class Client
      def initialize(destination, naming_host=localhost, naming_port=1099)
        @client = org.torquebox.ruby.enterprise.queues.RubyTaskQueueClient.new( naming_host, naming_port )
        @client.set_destination( destination )
      end

      def enqueue(task_name, payload=nil)
        @client.enqueue( task_name, payload ) 
      end

    end
  end
end
