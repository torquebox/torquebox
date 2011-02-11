
require 'org.torquebox.torquebox-container-foundation'
require 'org.torquebox.torquebox-naming-container'
require 'org.torquebox.torquebox-messaging-container'
require 'torquebox/messaging/destination'

describe TorqueBox::Messaging::Destination do

  it "should return its name for to_s" do
    queue = TorqueBox::Messaging::Queue.new("/queues/foo")
    queue.name.should == "/queues/foo"
    queue.to_s.should == queue.name
    topic = TorqueBox::Messaging::Topic.new("/topics/bar")
    topic.name.should == "/topics/bar"
    topic.to_s.should == topic.name
  end

  %w{ create start }.each do |method|

    it "should create a queue on #{method}" do
      server = mock("server")
      server.should_receive(:createQueue)
      server.should_receive(:destroyQueue).with("my_queue")
      TorqueBox::Kernel.stub!(:lookup).with("JMSServerManager").and_yield(server)

      queue = TorqueBox::Messaging::Queue.new("my_queue")
      queue.name.should eql("my_queue")
      queue.send method
      queue.destroy
    end

    it "should create a topic on #{method}" do
      server = mock("server")
      server.should_receive(:createTopic)
      server.should_receive(:destroyTopic).with("my_topic")
      TorqueBox::Kernel.stub!(:lookup).with("JMSServerManager").and_yield(server)

      topic = TorqueBox::Messaging::Topic.new("my_topic")
      topic.name.should eql("my_topic")
      topic.send method
      topic.destroy
    end

  end

  describe "publish" do
    before(:each) do
      @session = mock('session')
      @session.stub(:transacted?).and_return(false)
      TorqueBox::Messaging::Client.stub(:connect).and_yield(@session)
      @queue = TorqueBox::Messaging::Queue.new('foo')
    end

    context "normalizing options" do
      context "priority" do
        {
          :low => 1,
          :normal => 4,
          :high => 7,
          :critical => 9
        }.each do |symbol, level|
          it "should normalize :#{symbol} to #{level}" do
            @session.should_receive(:publish).with(anything, anything, { :priority => level })
            @queue.publish('message', { :priority => symbol })
          end
        end

        it "should pass through valid integer priorities" do
          @session.should_receive(:publish).with(anything, anything, { :priority => 5 })
          @queue.publish('message', { :priority => 5 })
        end

        it "should pass through valid integer-as-string priorities" do
          @session.should_receive(:publish).with(anything, anything, { :priority => 5 })
          @queue.publish('message', { :priority => "5" })
        end

        it "should raise on an invalid integer" do
          lambda { 
            @queue.publish('message', { :priority => -1 })
          }.should raise_error(ArgumentError)
        end
      end

      it "should handle persistence = true" do
        @session.should_receive(:publish).with(anything, anything, { :delivery_mode => javax.jms::DeliveryMode.PERSISTENT })
        @queue.publish('message', { :persistent => true })
      end

      it "should handle persistence = false" do
        @session.should_receive(:publish).with(anything, anything, { :delivery_mode => javax.jms::DeliveryMode.NON_PERSISTENT })
        @queue.publish('message', { :persistent => false })
      end
    end
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
      threads, count = [], 10

      # Use a threadsafe "array"
      msgs = java.util.Collections.synchronizedList( [] )

      # Ensure all clients are blocking on the receipt of a message
      count.times { threads << Thread.new { msgs << topic.receive } }
      sleep(1)
      topic.publish "howdy"
      threads.each {|t| t.join}

      topic.destroy
      msgs.to_a.should eql( ["howdy"] * count )
    end

    context "synchronous messaging" do
      it "should return value of block given to receive_and_publish" do
        queue = TorqueBox::Messaging::Queue.new "/queues/publish_and_receive"
        queue.start

        response_thread = Thread.new {
          queue.receive_and_publish( :timeout => 5000 ) { |msg| msg.upcase }
        }
        message = queue.publish_and_receive "ping", :timeout => 5000
        response_thread.join

        queue.destroy
        message.should eql( "PING" )
      end

      it "should return request message if no block given" do
        queue = TorqueBox::Messaging::Queue.new "/queues/publish_and_receive"
        queue.start

        response_thread = Thread.new {
          queue.receive_and_publish( :timeout => 5000 )
        }
        message = queue.publish_and_receive "ping", :timeout => 5000
        response_thread.join

        queue.destroy
        message.should eql( "ping" )
      end
    end
  end

end
