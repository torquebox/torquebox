require 'rubygems'
require 'org.torquebox.torquebox-messaging-client'

#TorqueBox::Naming.configure do |config|
  #config.host = "127.0.0.1"
  #config.port = 1099
#end

TorqueBox::Messaging::Queue.new('/queues/results').receive(:naming_host=>'127.0.0.1', :naming_port=>1099, :timeout => 2000)
