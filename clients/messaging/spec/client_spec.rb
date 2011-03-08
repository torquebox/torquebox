

require 'torquebox-messaging-container'
require 'torquebox-naming-container'
require 'torquebox-container-foundation'

require 'torquebox/messaging/client'

describe TorqueBox::Messaging::Client do

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

    it "should be able to send and receive a non-String message" do
      message = {:string => "a string", :symbol => :a_symbol, :hash => {}, :array => []}
      received_message = nil

      TorqueBox::Messaging::Client.connect() do |session|
        session.publish( '/queues/foo', message )
        session.commit
        received_message = session.receive( '/queues/foo' )
        session.commit
      end

      received_message.should_not be_nil
      received_message.should eql( message )
    end

    it "should identify a non-String message with a text property" do
      received_message = nil
      TorqueBox::Messaging::Client.connect() do |session|
        session.publish( '/queues/foo', [] )
        session.commit
        received_message = session.receive( '/queues/foo', :decode => false )
        session.commit
      end
      received_message.decode.should eql( [] )
      received_message.get_string_property( 'torquebox_encoding' ).should eql( 'base64' )
    end

    it "should not identify a String message with a text property" do
      received_message = nil
      TorqueBox::Messaging::Client.connect() do |session|
        session.publish( '/queues/foo', "foo" )
        session.commit
        received_message = session.receive( '/queues/foo', :decode => false )
        session.commit
      end
      received_message.text.should eql( "foo" )
      received_message.decode.should eql( "foo" )
      received_message.get_string_property( 'torquebox_encoding' ).should be_nil
    end

    it "should timeout if asked" do
      received_message = nil
      TorqueBox::Messaging::Client.connect() do |session|
        received_message = session.receive( '/queues/foo', :timeout => 1 )
      end
      received_message.should be_nil
    end
    
  end

end


