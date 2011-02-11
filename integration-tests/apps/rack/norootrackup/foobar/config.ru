
require 'org.torquebox.messaging-client'

TorqueBox::Messaging::Queue.new('/queues/foobar').publish("this won't work if queues.yml isn't in a metadata dir")

app = lambda { |env| [200, { 'Content-Type' => 'text/html' }, "RACK_ROOT=#{ENV['RACK_ROOT']}"] }
run app
