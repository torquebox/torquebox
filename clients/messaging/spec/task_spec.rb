
require 'torquebox/messaging/task'

class MyTestTask < TorqueBox::Messaging::Task 
  attr_accessor :payload
end

describe TorqueBox::Messaging::Task do

  it "should marshal payloads correctly" do
    TorqueBox::Messaging::Client.should_receive(:connect).and_yield(mock_session)
    MyTestTask.async(:payload=, :foo => 'bar')
    task = MyTestTask.new
    task.process! @message
    task.payload[:foo].should == 'bar'
  end

  it "should handle nil payload as empty hash" do
    TorqueBox::Messaging::Client.should_receive(:connect).and_yield(mock_session)
    MyTestTask.async(:payload=)
    task = MyTestTask.new
    task.process! @message
    task.payload.should be_empty
  end

  it "should derive the queue name from the class name" do
    MyTestTask.queue_name.should == "/queues/torquebox/tasks/mytest"
  end


  before(:each) do
    @message = Message.new
  end
  
  # Kinda hate that all this knowledge of JMS impl is required, but...
  def mock_session
    session = mock("session")
    producer = mock("producer")
    session.should_receive(:create_queue)
    session.should_receive(:create_producer).and_return(producer)
    session.should_receive(:create_text_message).and_return(@message)
    producer.should_receive(:send).with(@message)
    session.should_receive(:commit)
    session
  end

  class Message
    attr_accessor :text
    def initialize
      @property = {}
    end
    def set_string_property k, v
      raise "String required" unless k.kind_of? String and v.kind_of? String
      @property[k] = v
    end
    def get_string_property k
      @property[k]
    end
  end
      
end
