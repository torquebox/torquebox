require 'torquebox-messaging'
require 'torquebox/spec_helpers'

simple_queue = TorqueBox::Messaging.queue("queue/simple_queue",
                                          :durable => false)
sync_queue = TorqueBox::Messaging.queue("queue/synchronous_queue",
                                        :durable => false)
selector_queue = TorqueBox::Messaging.queue("queue/synchronous_with_selectors",
                                            :durable => false)
backchannel = TorqueBox::Messaging.queue("queue/backchannel")

simple_queue.listen do |message|
  backchannel.publish("#{message[:tstamp]} // #{message[:cheese]}")
end

responder = lambda do |message|
  "Got #{message} but I want bacon!"
end

sync_queue.respond(&responder)

selector = "awesomeness IS NOT NULL AND awesomeness > 10"
selector_queue.respond(:selector => selector, &responder)

TorqueBox::SpecHelpers.booted
