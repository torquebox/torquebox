CORE_DIR = "#{File.dirname(__FILE__)}/../../core"
$: << "#{CORE_DIR}/lib"
require "#{CORE_DIR}/spec/spec_helper"

require 'torquebox-messaging'

class GetBroker
  extend TorqueBox::Messaging::Helpers
end

RSpec.configure do |config|
  config.after(:suite) do
    GetBroker.send(:default_broker).stop
  end
end
