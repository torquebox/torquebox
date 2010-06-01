

puts "AAA"
require 'org.torquebox.torquebox-messaging-container'
require 'torquebox/container/messaging_enabler'
puts "BBB"

require 'org.torquebox.torquebox-container-foundation'
require 'torquebox/container/foundation'

require 'torquebox/messaging/client'

#require 'torquebox/container/messaging_enabler'

describe TorqueBox::Messaging::Client do

  before(:each) do
    puts "FOO #{org.torquebox.messaging.deployers.MessagingRbParsingDeployer}"
    @container = TorqueBox::Container::Foundation.new
    @container.enable( TorqueBox::Container::MessagingEnabler ) 
    @container.start
  end

  after(:each) do
    @container.stop
  end
  

  it "should be connectable" do
    client = TorqueBox::Messaging::Client.connect
  end

end


