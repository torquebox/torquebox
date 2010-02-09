class Java::org.torquebox.enterprise.ruby.messaging::Client
  def session(&block)
    instance_eval(&block) if block
  end
end

module TorqueBox
  module Messaging

    class Client 
      def self.new(&block)
        factory = Java::org.torquebox.enterprise.ruby.messaging::ClientFactory.new
        client = factory.create_client
        puts "client it #{client}"
        client.create
        client.start
        client.session(&block)
      end

    end

  end
end
