
require 'torquebox/messaging/queue'
require 'torquebox/messaging/topic'
require 'torquebox/service_registry'

java_import org.mockito.ArgumentCaptor
java_import org.mockito.Matchers
java_import org.mockito.Mockito
java_import org.hornetq.jms.server.impl.JMSServerManagerImpl

describe TorqueBox::Messaging::Destination do

  after(:each) do
    TorqueBox::Registry.registry.clear
  end

  it "should return its name for to_s" do
    queue = TorqueBox::Messaging::Queue.new("/queues/foo")
    queue.name.should == "/queues/foo"
    topic = TorqueBox::Messaging::Topic.new("/topics/bar")
    topic.name.should == "/topics/bar"
  end

  it "should fall back to internal connection factory" do
    factory = Object.new
    TorqueBox::Registry.merge!("connection-factory" => factory)
    queue = TorqueBox::Messaging::Queue.new("/queues/foo")
    queue.connection_factory.internal_connection_factory.should == factory
  end

  it "should initialize with connection factory if given" do
    factory = Object.new
    queue = TorqueBox::Messaging::Queue.new("/queues/foo", factory)
    queue.connection_factory.internal_connection_factory.should == factory
    queue.connect_options.should be_empty
  end

  it "should default to no connect options" do
    queue = TorqueBox::Messaging::Queue.new("/queues/foo")
    queue.connect_options.should be_empty
  end

  it "should initialize with connect options if given" do
    queue = TorqueBox::Messaging::Queue.new("/queues/foo", :host => "bart")
    queue.connect_options[:host].should == "bart"
  end

  it "should connect with host and port if given" do
    factory = mock("factory")
    connection = mock("connection").as_null_object
    factory.stub(:create_connection).and_return(connection)

    queue = TorqueBox::Messaging::Queue.new("/queues/foo", :host => "bar", :port => 123)
    queue.connection_factory.should_receive(:create_connection_factory).with("bar", 123).and_return(factory)
    queue.with_session { }
  end

  it "should connect with host and port if given even when inside container" do
    internal_factory = Object.new
    TorqueBox::Registry.merge!("connection-factory" => internal_factory)
    factory = mock("factory")
    connection = mock("connection").as_null_object
    factory.stub(:create_connection).and_return(connection)

    queue = TorqueBox::Messaging::Queue.new("/queues/foo", :host => "bar", :port => 123)
    queue.connection_factory.should_receive(:create_connection_factory).with("bar", 123).and_return(factory)
    queue.with_session { }
  end

  it "should connect with username and password if given" do
    factory = mock("factory")
    connection = mock("connection").as_null_object
    factory.should_receive(:create_connection).with("ham", "biscuit").and_return(connection)
    TorqueBox::Registry.merge!("connection-factory" => factory)

    queue = TorqueBox::Messaging::Queue.new("/queues/foo", :username => "ham", :password => "biscuit")
    queue.with_session { }
  end

  it "should return nil if the queue start times out" do
    latch = mock("StartLatch")
    latch.should_receive(:await).with(kind_of(Numeric), java.util.concurrent.TimeUnit::SECONDS).and_raise("boom")

    destinationizer = mock("Destinationizer")
    destinationizer.should_receive(:create_queue).with("my_queue", true, "", false).and_return(latch)

    TorqueBox::Messaging::Destination.should_receive(:with_destinationizer).and_yield(destinationizer)

    queue = TorqueBox::Messaging::Queue.start("my_queue")
    queue.should be_nil
  end

  it "should return nil if the topic start times out" do
    latch = mock("StartLatch")
    latch.should_receive(:await).with(kind_of(Numeric), java.util.concurrent.TimeUnit::SECONDS).and_raise("boom")

    destinationizer = mock("Destinationizer")
    destinationizer.should_receive(:create_topic).with("my_topic", false).and_return(latch)

    TorqueBox::Messaging::Destination.should_receive(:with_destinationizer).and_yield(destinationizer)

    topic = TorqueBox::Messaging::Topic.start( "my_topic" )
    topic.should be_nil
  end

  it "should start and stop a queue" do
    latch = mock("StartLatch")
    latch.should_receive(:await).with(kind_of(Numeric), java.util.concurrent.TimeUnit::SECONDS)

    destinationizer = mock("Destinationizer")
    destinationizer.should_receive(:create_queue).with("my_queue", true, "", false).and_return(latch)
    destinationizer.should_receive(:remove_destination).with("my_queue")

    TorqueBox::Messaging::Destination.should_receive(:with_destinationizer).twice.and_yield(destinationizer)

    queue = TorqueBox::Messaging::Queue.start("my_queue")
    queue.should_not be_nil
    queue.name.should == "my_queue"
    queue.stop
  end

  it "should start and stop a topic" do
    latch = mock("StartLatch")
    latch.should_receive(:await).with(kind_of(Numeric), java.util.concurrent.TimeUnit::SECONDS)

    destinationizer = mock("Destinationizer")
    destinationizer.should_receive(:create_topic).with("my_topic", false).and_return(latch)
    destinationizer.should_receive(:remove_destination).with("my_topic")

    TorqueBox::Messaging::Destination.should_receive(:with_destinationizer).twice.and_yield(destinationizer)

    topic = TorqueBox::Messaging::Topic.start( "my_topic" )
    topic.name.should == "my_topic"
    topic.stop
  end

  it "should start a queue and stop it in an synchronous way" do
    start_latch = mock("StartLatch")
    start_latch.should_receive(:await).with(kind_of(Numeric), java.util.concurrent.TimeUnit::SECONDS)

    stop_latch = mock("StopLatch")
    stop_latch.should_receive(:await).with(kind_of(Numeric), java.util.concurrent.TimeUnit::SECONDS)

    destinationizer = mock("Destinationizer")
    destinationizer.should_receive(:create_queue).with("my_queue", true, "", false).and_return(start_latch)
    destinationizer.should_receive(:remove_destination).with("my_queue").and_return(stop_latch)

    TorqueBox::Messaging::Destination.should_receive(:with_destinationizer).twice.and_yield(destinationizer)

    queue = TorqueBox::Messaging::Queue.start("my_queue")
    queue.should_not be_nil
    queue.name.should == "my_queue"
    queue.stop_sync
  end

  it "should start a topic and stop it in an synchronous way" do
    start_latch = mock("StartLatch")
    start_latch.should_receive(:await).with(kind_of(Numeric), java.util.concurrent.TimeUnit::SECONDS)

    stop_latch = mock("StopLatch")
    stop_latch.should_receive(:await).with(kind_of(Numeric), java.util.concurrent.TimeUnit::SECONDS)

    destinationizer = mock("Destinationizer")
    destinationizer.should_receive(:create_topic).with("my_topic", false).and_return(start_latch)
    destinationizer.should_receive(:remove_destination).with("my_topic").and_return(stop_latch)

    TorqueBox::Messaging::Destination.should_receive(:with_destinationizer).twice.and_yield(destinationizer)

    topic = TorqueBox::Messaging::Topic.start( "my_topic" )
    topic.name.should == "my_topic"
    topic.stop_sync
  end

  it "should raise ArgumentError if destination is nil" do
    lambda {
      TorqueBox::Messaging::Queue.new( nil )
    }.should raise_error( ArgumentError )
    lambda {
      TorqueBox::Messaging::Topic.new( nil )
    }.should raise_error( ArgumentError )
  end

  describe "publish" do
    before(:each) do
      @session = mock('session')
      @session.stub(:transacted?).and_return(false)
      @queue = TorqueBox::Messaging::Queue.new('foo')
      @queue.stub(:with_session).and_yield(@session)
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

  context "queue management" do
    before(:each) do
      jms_manager = mock('JMSManager')
      @server_control = mock('ServerControl')

      TorqueBox::ServiceRegistry.stub!(:lookup).with("jboss.messaging.default.jms.manager").and_yield(jms_manager)
      Java::org.hornetq.jms.management.impl.JMSServerControlImpl.stub!(:new).with(jms_manager).and_return(@server_control)
    end

    describe "lookup" do
      it "should return nil lookup if queue unavailable" do
        @server_control.should_receive(:queue_names).and_return([])
        TorqueBox::Messaging::Queue.lookup('/queues/doesntexist').should be_nil
      end
    end

    describe "list" do
      before(:each) do
        jms_manager = mock('JMSManager')
        @server_control = mock('ServerControl')

        TorqueBox::Registry.merge!("connection-factory" => Object.new)
        TorqueBox::Registry.merge!("transaction-manager" => Object.new)
        TorqueBox::ServiceRegistry.stub!(:lookup).with("jboss.messaging.default.jms.manager").and_yield(jms_manager)
        Java::org.hornetq.jms.management.impl.JMSServerControlImpl.stub!(:new).with(jms_manager).and_return(@server_control)
      end

      it "should return empty list if no queues are available" do
        @server_control.should_receive(:queue_names).and_return([])

        TorqueBox::Messaging::Queue.list.should == []
      end

      it "should return empty list if no topics are available" do
        @server_control.should_receive(:topic_names).and_return([])

        TorqueBox::Messaging::Topic.list.should == []
      end

      it "should return list with available queues" do
        @server_control.should_receive(:queue_names).and_return(['/queues/one', '/queues/two'])

        queues = TorqueBox::Messaging::Queue.list
        queues.size.should == 2
        queues[0].name.should == '/queues/one'
        queues[1].name.should == '/queues/two'
      end

      it "should return list with available topics" do
        @server_control.should_receive(:topic_names).and_return(['/topics/one', '/topics/two'])

        topics = TorqueBox::Messaging::Topic.list
        topics.size.should == 2
        topics[0].name.should == '/topics/one'
        topics[1].name.should == '/topics/two'
      end
    end
  end
end
