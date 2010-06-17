
FUCK FUCK FUCK{
class MyConsumer
  def process!(msg)
    puts "processing #{msg.inspect}"
  end
end

TorqueBox::Messaging::Gateway.define do |gateway|
  gateway.subscribe MyConsumer, '/queues/foo'
end
