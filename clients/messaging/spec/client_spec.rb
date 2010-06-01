

require 'org.torquebox.torquebox-messaging-container'
require 'org.torquebox.torquebox-naming-container'
require 'org.torquebox.torquebox-container-foundation'

require 'torquebox/container/foundation'
require 'torquebox/container/naming_enabler'
require 'torquebox/container/messaging_enabler'

require 'torquebox/messaging/client'

describe TorqueBox::Messaging::Client do

  describe "basics" do 
    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @container.enable( TorqueBox::Container::NamingEnabler ) 
      @container.enable( TorqueBox::Container::MessagingEnabler ) 
      @container.start
    end
  
    after(:each) do
      @container.stop
    end
  
    it "should be connectable" do
      TorqueBox::Messaging::Client.connect do |session|
        session.should_not be_nil
      end
    end
  end

  describe "sending and receiving" do
    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @container.enable( TorqueBox::Container::NamingEnabler ) 
      @container.enable( TorqueBox::Container::MessagingEnabler ) 
      @container.start

      @queues_yml = @container.deploy( File.join( File.dirname(__FILE__), 'queues.yml' ) )
      @container.process_deployments(true)
    end
  
    after(:each) do
      @container.undeploy( @queues_yml )
      @container.stop
    end

    it "should be able to send to a queue" do
      TorqueBox::Messaging::Client.connect do |session|
        session.publish( '/queues/foo', "howdy" )
      end
    end
  
  end

end


