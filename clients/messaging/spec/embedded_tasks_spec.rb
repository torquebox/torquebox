require 'torquebox/messaging/embedded_tasks'

class MyTestModel
  extend TorqueBox::Messaging::EmbeddedTasks

  def an_async_action(arg1, arg2)
    a_sync_action
  end
  handle_async :an_async_action

  def a_sync_action;  end
  def foo;  end
  def bar;  end
end

describe TorqueBox::Messaging::EmbeddedTasks do

  describe "handle_async" do
    it "should be able to handle mutliple methods" do
      MyTestModel.handle_async :foo, :bar
      instance_methods = MyTestModel.instance_methods
      instance_methods.should include('__async_foo')
      instance_methods.should include('__async_bar')
    end
  end

  describe "a method handled asynchronously" do
    before(:each) do
      @queue = mock('queue')
      @queue.stub(:publish)
      TorqueBox::Messaging::Queue.stub(:new).and_return(@queue)
    end
    
    it "should put a message on the queue" do
      @queue.should_receive(:publish)
      MyTestModel.new.an_async_action(nil, nil)
    end

    it "should include the receiver, sync method, and args" do
      object = MyTestModel.new
      @queue.should_receive(:publish).with(:receiver => object, :method => '__sync_an_async_action', :args => [:a, :b])
      object.an_async_action(:a, :b)
    end
    
    it "should not call the action immediately" do
      @object = MyTestModel.new
      @object.should_not_receive(:a_sync_action)
      @object.an_async_action(nil, nil)
    end
  end
end
