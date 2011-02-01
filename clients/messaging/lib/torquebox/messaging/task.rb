require 'torquebox/messaging/destination'

module TorqueBox
  module Messaging

    class Task

      def self.queue_name
        "/queues/torquebox/#{ENV['TORQUEBOX_APP_NAME']}/tasks/#{name[0...-4].downcase}"
      end

      def self.async(method, payload={})
        message = {:method => method, :payload => payload}
        Queue.new(queue_name).publish message
      end

      def process!(message)
        hash = message.decode
        self.send hash[:method].to_sym, hash[:payload]
      end

    end

  end
end

