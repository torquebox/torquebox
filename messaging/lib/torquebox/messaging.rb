require 'torquebox/messaging/connection'
require 'torquebox/messaging/destination'
require 'torquebox/messaging/queue'
require 'torquebox/messaging/topic'
require 'torquebox/messaging/message'
require 'torquebox/messaging/session'

module TorqueBox
  module Messaging
    def self.default_encoding
      @default_encoding ||= :marshal
    end

    def self.default_encoding=(enc)
      @default_encoding = enc
    end
  end
end
