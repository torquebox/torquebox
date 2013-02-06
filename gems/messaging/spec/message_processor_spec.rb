require 'torquebox/messaging/message_processor'
require 'torquebox/messaging/message'
require 'torquebox/messaging/marshal_base64_message'

class MyTestMessage
  include javax.jms::TextMessage
  attr_accessor :text
  def initialize 
    @properties = {}
  end
  def set_string_property k, v
    @properties[k] = v
  end
  def get_string_property k
    @properties[k]
  end
end

class MyTestSession
  include javax.jms::Session

  def create_text_message
    MyTestMessage.new
  end
end

class MyTestProcessor < TorqueBox::Messaging::MessageProcessor
  attr_accessor :body
  def on_message(body)
    self.body = body
  end
end

describe TorqueBox::Messaging::MessageProcessor do
  
  before :each do
    @processor = MyTestProcessor.new
    @jms_session = MyTestSession.new
  end

  it "should process text messages" do
    @message = TorqueBox::Messaging::Message.new(@jms_session, "foo", :marshal_base64)
    @processor.process! @message
    @processor.body.should eql("foo")
  end

  it "should process non-text messages" do
    payload = {:foo => "foo", :sym => :sym, "bar" => :bar}
    @message = TorqueBox::Messaging::Message.new(@jms_session, payload, :marshal_base64)
    @processor.process! @message
    @processor.body.should eql(payload)
  end

  describe "#middleware" do
    it "should return the default middleware" do
      @processor.middleware.inspect.should == "[TorqueBox::Messaging::ProcessorMiddleware::WithTransaction]"
    end
  end

  context "lookups" do
    class ServiceNameMock
      def initialize(name)
        @name = name
      end

      def append(name)
        @name << ".#{name}"
        self
      end

      def canonical_name
        @name
      end
    end

    before(:each) do
      @service_name = ServiceNameMock.new("jboss.deployment.unit.\"test-message_processor_concurrency-knob.yml\"")

      TorqueBox::MSC.stub_chain(:deployment_unit, :service_name).and_return(@service_name)
    end

    describe ".list" do
      class ServiceMock
        def initialize(name)
          @name = name
        end

        attr_reader :name

        def value
          self
        end
      end

      it "should return list of avaialble message processors" do
        TorqueBox::MSC.should_receive(:get_services).
          with(/^jboss.deployment.unit."test-message_processor_concurrency-knob.yml".torquebox.messaging\.\".*\"$/).
          and_yield(ServiceMock.new("jboss.deployment.unit.\"test-message_processor_concurrency-knob.yml\".torquebox.messaging"))

        processors = TorqueBox::Messaging::MessageProcessor.list

        processors.should_not == nil
        processors.size.should == 1
      end

      it "should return empty list when no message processors are available" do
        TorqueBox::MSC.should_receive(:get_services).with(/^jboss.deployment.unit."test-message_processor_concurrency-knob.yml".torquebox.messaging\.\".*\"$/)

        processors = TorqueBox::Messaging::MessageProcessor.list

        processors.should_not == nil
        processors.size.should == 0
      end
    end

    describe ".lookup" do
      it "should return a message processor" do
        TorqueBox::ServiceRegistry.should_receive(:lookup).with(@service_name).and_return(mock('Group'))

        processor = TorqueBox::Messaging::MessageProcessor.lookup('/queues/sample', 'SimpleProcessor')
        processor.should_not == nil

        @service_name.canonical_name.should match(/torquebox\.messaging\.\/queues\/sample\.SimpleProcessor$/)
      end

      it "should return nil when a message processor is not found" do
        TorqueBox::ServiceRegistry.should_receive(:lookup).with(@service_name)

        processor = TorqueBox::Messaging::MessageProcessor.lookup('/queues/sample', 'SimpleProcessor')
        processor.should == nil

        @service_name.canonical_name.should match(/torquebox\.messaging\.\/queues\/sample\.SimpleProcessor$/)
      end
    end
  end
end

describe TorqueBox::Messaging::MessageProcessorProxy do
  describe ".new" do
    it "should not raise when creating the object" do
      lambda {
        MessageProcessorProxy.new("group")
      }.should_not raise_error
    end

    it "should raise when the group is nil" do
      lambda {
        MessageProcessorProxy.new(nil)
      }.should raise_error(Exception, "Cannot create MessageProcessorProxy for non-existing MessageProcessorGroup")
    end
  end

  context "management" do
    before :each do
      @group = mock('BaseMessageProcessorGroup')
      @proxy = MessageProcessorProxy.new(@group)
    end

    it "should return group properties" do
      @group.should_receive(:concurrency).and_return(3)
      @group.should_receive(:message_selector).and_return("something is null")
      @group.should_receive(:message_processor_class).and_return(mock("Class", :name => 'SimpleProcessor'))
      @group.should_receive(:destination_name).and_return("/queues/simple")
      @group.should_receive(:name).and_return("/queues/simple.SimpleProcessor")
      @group.should_receive(:durable).and_return(false)

      @proxy.concurrency.should == 3
      @proxy.message_selector.should == "something is null"
      @proxy.class_name.should == "SimpleProcessor"
      @proxy.destination_name.should == "/queues/simple"
      @proxy.name.should == "/queues/simple.SimpleProcessor"
      @proxy.durable?.should == false
    end

    describe ".concurrency=" do
      it "should change the concurrency" do
        @group.should_receive(:concurrency).and_return(1)
        @group.should_receive(:concurrency).and_return(3)
        @group.should_receive(:update_concurrency).with(3).and_return(3)

        (@proxy.concurrency = 3).should == 3
      end

      it "should allow to set the concurrency to 0" do
        @group.should_receive(:concurrency).and_return(1)
        @group.should_receive(:concurrency).and_return(0)
        @group.should_receive(:update_concurrency).with(0).and_return(0)

        (@proxy.concurrency = 0).should == 0
      end

      it "should do nothing if the concurrency size is the same" do
        @group.should_receive(:concurrency).and_return(3)
        @group.should_not_receive(:update_concurrency)

        (@proxy.concurrency = 3).should == 3
      end

      it "should raise if the value is lower than 0" do
        @group.should_receive(:name).and_return("/queues/simple.SimpleProcessor")
        @group.should_not_receive(:update_concurrency)

        lambda {
          @proxy.concurrency = -2
        }.should raise_error(Exception, "Setting concurrency for '/queues/simple.SimpleProcessor' to value < 0 is not allowed. You tried '-2'.")
      end
    end
  end
end
