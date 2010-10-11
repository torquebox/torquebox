require 'rubygems'
require 'org.torquebox.torquebox-messaging-client'

result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 2000)

puts "result from receive is #{result.inspect}"

result
