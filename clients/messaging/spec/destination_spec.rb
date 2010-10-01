
require 'org.torquebox.torquebox-container-foundation'
require 'org.torquebox.torquebox-naming-container'
require 'org.torquebox.torquebox-messaging-container'
require 'torquebox/messaging/destination'

describe TorqueBox::Messaging::Destination do

  before(:each) do

  end
  
  it "should create a queue when started" do
    server = mock("server")
    server.should_receive(:createQueue)
    server.should_receive(:destroyQueue).with("my_queue")
    TorqueBox::Kernel.stub!(:lookup).with("JMSServerManager").and_yield(server)

    queue = TorqueBox::Messaging::Queue.new("my_queue")
    queue.name.should eql("my_queue")
    queue.start
    queue.destroy
  end

  it "should create a topic when started" do
    server = mock("server")
    server.should_receive(:createTopic)
    server.should_receive(:destroyTopic).with("my_topic")
    TorqueBox::Kernel.stub!(:lookup).with("JMSServerManager").and_yield(server)

    topic = TorqueBox::Messaging::Topic.new("my_topic")
    topic.name.should eql("my_topic")
    topic.start
    topic.destroy
  end

  describe "sending and receiving" do
    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @container.enable( TorqueBox::Naming::NamingService ) {|config| config.export=false}
      @container.enable( TorqueBox::Messaging::MessageBroker ) 
      @container.start
    end
  
    after(:each) do
      @container.stop
    end

    it "should be able to publish to and receive from a queue" do
      queue = TorqueBox::Messaging::Queue.new "/queues/foo"
      queue.start

      queue.publish "howdy"
      message = queue.receive

      queue.destroy
      message.should eql( "howdy" )
    end

    it "should publish to multiple topic consumers" do
      topic = TorqueBox::Messaging::Topic.new "/topics/foo"
      topic.start
      threads, done, count = [], false, 10

      # Use a threadsafe "array"
      msgs = java.util.Collections.synchronizedList( [] )

      # Ensure all clients are blocking on the receipt of a message
      count.times { threads << Thread.new { msgs << topic.receive } }
      reaper = Thread.new { threads.each {|t| t.join}; done = true }

      # Keep broadcasting until the reaper has claimed all consumers
      until done
        topic.publish "howdy"
      end

      topic.destroy
      msgs.to_a.should eql( ["howdy"] * count )
    end
  end

end
