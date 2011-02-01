
require 'torquebox/messaging/task'

class MyTestTask < TorqueBox::Messaging::Task 
  attr_accessor :payload
end

describe TorqueBox::Messaging::Task do

  it "should send payload correctly" do
    expectation = {:method => :payload=, :payload => {:foo => 'bar'}}
    queue = mock("queue")
    queue.should_receive(:publish).with(expectation) 
    TorqueBox::Messaging::Queue.should_receive(:new).with(MyTestTask.queue_name).and_return(queue)

    MyTestTask.async(:payload=, :foo => 'bar')
  end

  it "should process payload correctly" do
    expectation = {:method => :payload=, :payload => {:foo => 'bar'}}
    message = mock("message")
    message.should_receive(:decode).and_return(expectation)

    task = MyTestTask.new
    task.process! message
    task.payload[:foo].should == 'bar'
  end

  it "should handle nil payload as empty hash" do
    queue = mock("queue")
    queue.should_receive(:publish).with(hash_including(:payload => {})) 
    TorqueBox::Messaging::Queue.should_receive(:new).with(MyTestTask.queue_name).and_return(queue)
    MyTestTask.async(:payload=)
  end

  it "should derive the queue name from the class name" do
    MyTestTask.queue_name.should =~ %r{/mytest$}
  end

  it "should include the app name in the queue name" do
    ENV['TORQUEBOX_APP_NAME'] = 'app_name'
    MyTestTask.queue_name.should =~ %r{/app_name/}
  end
end
