
require 'torquebox-container-foundation'
require 'torquebox-naming-container'
require 'torquebox-messaging-container'
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


  describe "message selectors" do
    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @container.enable( TorqueBox::Naming::NamingService ) {|config| config.export=false}
      @container.enable( TorqueBox::Messaging::MessageBroker )
      @container.start
      @queue = TorqueBox::Messaging::Queue.new "/queues/selector_test"
      @queue.start
    end

    after(:each) do
      @queue.destroy
      @container.stop
    end

    {
      true => 'prop = true',
      true => 'prop <> false',
      5 => 'prop = 5',
      5 => 'prop > 4',
      5.5 => 'prop = 5.5',
      5.5 => 'prop < 6',
      'string' => "prop = 'string'"
    }.each do |value, selector|
      it "should be able to select with property set to #{value} using selector '#{selector}'" do
        @queue.publish value.to_s, :properties => { :prop => value }
        message = @queue.receive(:timeout => 1000, :selector => selector)
        message.should == value.to_s
      end
    end
    
  end
  
  describe "browse" do
    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @container.enable( TorqueBox::Naming::NamingService ) {|config| config.export=false}
      @container.enable( TorqueBox::Messaging::MessageBroker )
      @container.start
    end

    after(:each) do
      @container.stop
    end

    it "should allow enumeration of the messages" do
      queue = TorqueBox::Messaging::Queue.new "/queues/browseable"
      queue.start
      queue.publish "howdy"
      queue.first.text.should == 'howdy'
      queue.destroy
    end

    it "should accept a selector" do
      queue = TorqueBox::Messaging::Queue.new "/queues/browseable", {}, :selector => 'blurple > 5'
      queue.start
      queue.publish "howdy", :properties => {:blurple => 5}
      queue.publish "ahoyhoy", :properties => {:blurple => 6}
      queue.first.text.should == 'ahoyhoy'
      queue.detect { |m| m.text == 'howdy' }.should be_nil
      queue.destroy
      
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
          queue.receive_and_publish( :timeout => 10000 ) { |msg| msg.upcase }
        }
        message = queue.publish_and_receive "ping", :timeout => 10000
        response_thread.join

        queue.destroy
        message.should eql( "PING" )
      end

      it "should return request message if no block given" do
        queue = TorqueBox::Messaging::Queue.new "/queues/publish_and_receive"
        queue.start

        response_thread = Thread.new {
          queue.receive_and_publish( :timeout => 10000 )
        }
        message = queue.publish_and_receive "ping", :timeout => 10000
        response_thread.join

        queue.destroy
        message.should eql( "ping" )
      end

      it "should not mess up with multiple consumers" do
        queue = TorqueBox::Messaging::Queue.new "/queues/publish_and_receive"
        queue.start

        thread_count = 3
        response_threads = (1..thread_count).map do
          Thread.new {
            queue.receive_and_publish( :timeout => 10000 ) { |msg| msg.upcase }
          }
        end

        message = queue.publish_and_receive "ping", :timeout => 10000
        # Send extra messages to trigger all remaining response threads
        (thread_count - 1).times do
          queue.publish_and_receive "ping", :timeout => 10000
        end
        response_threads.each { |thread| thread.join }

        queue.destroy
        message.should eql( "PING" )
      end
    end

    context "destination not ready" do
      it "should block on publish until queue is ready" do
        queue = TorqueBox::Messaging::Queue.new "/queues/not_ready"
        # Start the queue in a separate thread after a delay
        setup_thread = Thread.new {
          sleep( 0.2 )
          queue.start
        }
        # The queue will not be ready when we call the publish method
        queue.publish "something"
        message = queue.receive

        setup_thread.join
        queue.destroy
        message.should eql( "something" )
      end

      it "should block on receive until topic is ready" do
        topic = TorqueBox::Messaging::Topic.new "/topics/not_ready"
        # Start the topic in a separate thread after a delay
        setup_thread = Thread.new {
          topic.start
        }
        # The topic will not be ready when we call the receive method
        message = topic.receive :timeout => 200

        setup_thread.join
        topic.destroy
        message.should be_nil
      end

      it "should block until startup_timeout reached" do
        queue = TorqueBox::Messaging::Queue.new "/queues/not_ready"
        lambda {
          queue.publish "something", :startup_timeout => 200
        }.should raise_error(javax.naming.NameNotFoundException)
      end
    end
  end

end
