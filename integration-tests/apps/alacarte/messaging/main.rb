require 'torquebox-messaging'
require 'torquebox/spec_helpers'

simple_queue = TorqueBox::Messaging.queue("queue/simple_queue", :durable => false)
backchannel = TorqueBox::Messaging.queue('queue/backchannel')

simple_queue.listen do |message|
  backchannel.publish('release')
end

TorqueBox::SpecHelpers.booted
