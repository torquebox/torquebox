
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

end
