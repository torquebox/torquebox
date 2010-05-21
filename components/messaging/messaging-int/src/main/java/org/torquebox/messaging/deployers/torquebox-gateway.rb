
require 'torquebox-messaging-metadata-builder'

module TorqueBox
  module Messaging
    class Gateway
      def self.define(&block)
        builder = TorqueBox::Messaging::MetaData::Builder.new(&block)
        builder.processors
      end
    end
  end
end