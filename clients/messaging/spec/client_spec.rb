

require 'org.torquebox.torquebox-messaging-container'
require 'org.torquebox.torquebox-naming-container'
require 'org.torquebox.torquebox-container-foundation'

require 'torquebox/messaging/client'

describe TorqueBox::Messaging::Client do

=begin
  describe "basics" do 
    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @container.enable( TorqueBox::Naming::NamingService ) {|config| config.export=false}
      @container.enable( TorqueBox::Messaging::MessageBroker ) 
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
=end

  describe "sending and receiving" do
    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @container.enable( TorqueBox::Naming::NamingService ) {|config| config.export=false}
      @container.enable( TorqueBox::Messaging::MessageBroker ) 
      @container.start

      @queues_yml = @container.deploy( File.join( File.dirname(__FILE__), 'queues.yml' ) )
      @container.process_deployments(true)
    end
  
    after(:each) do
      @container.undeploy( @queues_yml )
      @container.stop
    end

    it "should be able to send to a queue from two threads/two sessions" do
  
      received_message = nil

      consumer_thread = Thread.new {
        sleep( 2 )
        TorqueBox::Messaging::Client.connect() do |session|
          received_message = session.receive( '/queues/foo' )
          session.commit
        end
      }

      producer_thread = Thread.new {
        TorqueBox::Messaging::Client.connect() do |session|
          session.publish( '/queues/foo', "howdy" )
          session.commit
        end
      }

      consumer_thread.join

      received_message.should_not be_nil
      received_message.should eql( "howdy" )
    end


    it "should be able to send to a queue from one thread/one session" do

      received_message = nil

      TorqueBox::Messaging::Client.connect() do |session|
        session.publish( '/queues/foo', "howdy" )
        session.commit
        received_message = session.receive( '/queues/foo' )
        session.commit
      end

      received_message.should_not be_nil
      received_message.should eql( "howdy" )
    end
  
  end

end


