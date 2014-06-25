CORE_DIR = "#{File.dirname(__FILE__)}/../../core"
$: << "#{CORE_DIR}/lib"
require "#{CORE_DIR}/spec/spec_helper"

require 'torquebox-messaging'

java_import java.util.concurrent.CountDownLatch
java_import java.util.concurrent.TimeUnit

class GetBroker
  extend TorqueBox::Messaging::Helpers
end

def random_queue
  TorqueBox::Messaging::Queue.new(SecureRandom.uuid, durable: false)
end

RSpec.configure do |config|
  config.after(:suite) do
    GetBroker.send(:default_broker).stop
  end
end
