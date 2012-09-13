require 'torquebox/messaging/backgroundable'
require 'pp'

class InstanceMethodTestModel
  include TorqueBox::Messaging::Backgroundable

  def an_async_action(arg1, arg2)
    a_sync_action
  end
  always_background :an_async_action

  def a_sync_action;  end
  def foo;  end
  def bar;  end
  def optioned; end
  def redefine_me; end
end

class ClassMethodTestModel
  include TorqueBox::Messaging::Backgroundable

  def self.an_async_action(arg1, arg2)
    a_sync_action
  end
  always_background :an_async_action

  def self.a_sync_action;  end
  def self.foo;  end
  def self.bar;  end
  def self.optioned; end
  def self.redefine_me; end

  def foo; end

  class << self
    def singleton_class
      self
    end
  end
end

describe TorqueBox::Messaging::Backgroundable do
  before(:all) do
    @util = TorqueBox::Messaging::Backgroundable::Util
  end

  describe "always_background" do
    context "for instance methods" do
      it "should be able to handle multiple methods" do
        InstanceMethodTestModel.always_background :foo, :bar
        @util.instance_methods_include?(InstanceMethodTestModel, '__async_foo').should be_true
        @util.instance_methods_include?(InstanceMethodTestModel, '__async_bar').should be_true
      end

      it "should handle methods that are defined after the always_background call" do
        InstanceMethodTestModel.always_background :baz
        @util.instance_methods_include?(InstanceMethodTestModel, '__async_baz').should_not be_true
        InstanceMethodTestModel.class_eval('def baz;end')
        @util.instance_methods_include?(InstanceMethodTestModel, '__async_baz').should be_true
      end

      it "should handle methods that are redefined after the always_background call" do
        InstanceMethodTestModel.always_background :redefine_me
        InstanceMethodTestModel.class_eval('def redefine_me; :xyz; end')
        InstanceMethodTestModel.new.__sync_redefine_me.should == :xyz
      end

      it "should work for private methods, maintaining visibility" do
        InstanceMethodTestModel.class_eval('private; def no_peeking;end')
        InstanceMethodTestModel.always_background :no_peeking
        @util.private_instance_methods_include?(InstanceMethodTestModel, 'no_peeking').should be_true
        @util.private_instance_methods_include?(InstanceMethodTestModel, '__async_no_peeking').should be_true
        @util.private_instance_methods_include?(InstanceMethodTestModel, '__sync_no_peeking').should be_true
      end

      it "should work for protected methods, maintaining visibility" do
        InstanceMethodTestModel.class_eval('protected; def some_peeking;end')
        InstanceMethodTestModel.always_background :some_peeking
        @util.protected_instance_methods_include?(InstanceMethodTestModel, 'some_peeking').should be_true
        @util.protected_instance_methods_include?(InstanceMethodTestModel, '__async_some_peeking').should be_true
        @util.protected_instance_methods_include?(InstanceMethodTestModel, '__sync_some_peeking').should be_true
      end
    end

    context "for class methods" do
      it "should work when called after the method is defined" do
        @util.singleton_methods_include?(ClassMethodTestModel, '__async_an_async_action').should be_true
      end

      it "should be able to handle multiple methods" do
        ClassMethodTestModel.always_background :foo, :bar
        @util.singleton_methods_include?(ClassMethodTestModel, '__async_foo').should be_true
        @util.singleton_methods_include?(ClassMethodTestModel, '__async_bar').should be_true
      end

      it "class methods should win a tie" do
        ClassMethodTestModel.always_background :foo
        @util.singleton_methods_include?(ClassMethodTestModel, '__async_foo').should be_true
        @util.instance_methods_include?(ClassMethodTestModel, '__async_foo').should_not be_true
      end

      it "should handle methods that are defined after the always_background call" do
        ClassMethodTestModel.always_background :baz
        @util.singleton_methods_include?(ClassMethodTestModel, '__async_baz').should_not be_true
        ClassMethodTestModel.class_eval('def self.baz;end')
        @util.singleton_methods_include?(ClassMethodTestModel, '__async_baz').should be_true
      end

      it "should handle methods that are redefined after the always_background call" do
        ClassMethodTestModel.always_background :redefine_me
        ClassMethodTestModel.class_eval('def self.redefine_me; :xyz; end')
        ClassMethodTestModel.__sync_redefine_me.should == :xyz
      end

      it "should work for private methods, maintaining visibility" do
        ClassMethodTestModel.class_eval do
          class << self
            private
            def no_peeking;end
          end
        end

        ClassMethodTestModel.always_background :no_peeking

        @util.private_singleton_methods_include?(ClassMethodTestModel,
                                                 'no_peeking').should be_true
        @util.private_singleton_methods_include?(ClassMethodTestModel,
                                                 '__async_no_peeking').should be_true
        @util.private_singleton_methods_include?(ClassMethodTestModel,
                                                 '__sync_no_peeking').should be_true
      end

      it "should work for protected methods, maintaining visibility" do
        ClassMethodTestModel.class_eval do
          class << self
            protected
            def some_peeking;end
          end
        end

        ClassMethodTestModel.always_background :some_peeking

        @util.protected_singleton_methods_include?(ClassMethodTestModel,
                                                   'some_peeking').should be_true
        @util.protected_singleton_methods_include?(ClassMethodTestModel,
                                                   '__async_some_peeking').should be_true
        @util.protected_singleton_methods_include?(ClassMethodTestModel,
                                                   '__sync_some_peeking').should be_true
      end
    end

  end

  describe "a method handled asynchronously" do
    before(:each) do
      @queue = mock('queue')
      @queue.stub(:publish)
      TorqueBox::Messaging::Queue.stub(:new).and_return(@queue)
      TorqueBox::Messaging::Future.stub(:unique_id).and_return('1234')
    end

    it "should put a message on the queue" do
      @queue.should_receive(:publish)
      InstanceMethodTestModel.new.an_async_action(nil, nil)
    end

    it "should return a future" do
      result = InstanceMethodTestModel.new.an_async_action(nil, nil)
      result.is_a?(TorqueBox::Messaging::Future).should be_true
    end

    it "should include the proper options in the message" do
      object = InstanceMethodTestModel.new
      @queue.should_receive(:publish).with({
                                             :receiver => object,
                                             :future_id => '1234',
                                             :future_queue => "/queues/torquebox//tasks/torquebox_backgroundable",
                                             :method => '__sync_an_async_action',
                                             :args => [:a, :b]
                                           },
                                           anything)
      object.an_async_action(:a, :b)
    end

    it "should not call the action immediately" do
      object = InstanceMethodTestModel.new
      object.should_not_receive(:a_sync_action)
      object.an_async_action(nil, nil)
    end

    it "should pass through the options" do
      InstanceMethodTestModel.always_background :optioned, :priority => :low
      @queue.should_receive(:publish).with(anything, hash_including(:priority => :low))
      InstanceMethodTestModel.new.optioned
    end

  end

  describe 'background' do
    context "for instance methods" do
      before(:each) do
        @queue = mock('queue')
        @queue.stub(:publish)
        TorqueBox::Messaging::Queue.stub(:new).and_return(@queue)
        @object = InstanceMethodTestModel.new
      end

      it "should queue any method called on it" do
        @queue.should_receive(:publish).with(hash_including(:method => :foo),
                                             anything)
        @object.background.foo
      end

      it "should queue the receiver" do
        @queue.should_receive(:publish).with(hash_including(:receiver => @object), anything)
        @object.background.foo
      end

      it "should queue the args" do
        @queue.should_receive(:publish).with(hash_including(:args => [1,2]), anything)
        @object.background.foo(1,2)
      end

      it "should raise when given a block" do
        lambda {
          @object.background.foo { }
        }.should raise_error(ArgumentError)
      end

      it "should handle missing methods" do
        @object.should_receive(:method_missing)
        @object.background.no_method
      end

      it "should pass through any options" do
        @queue.should_receive(:publish).with(anything,
                                             hash_including(:ttl => 1))
        @object.background(:ttl => 1).foo
      end

      it "should always use the :marshal encoding" do
        @queue.should_receive(:publish).with(anything,
                                             hash_including(:encoding => :marshal))
        @object.background.foo
      end

    end

    context "for class methods" do
      before(:each) do
        @queue = mock('queue')
        @queue.stub(:publish)
        TorqueBox::Messaging::Queue.stub(:new).and_return(@queue)
        @object = ClassMethodTestModel
      end

      it "should queue any method called on it" do
        @queue.should_receive(:publish).with(hash_including(:method => :foo),
                                             anything)
        @object.background.foo
      end

      it "should queue the receiver" do
        @queue.should_receive(:publish).with(hash_including(:receiver => @object), anything)
        @object.background.foo
      end

      it "should queue the args" do
        @queue.should_receive(:publish).with(hash_including(:args => [1,2]), anything)
        @object.background.foo(1,2)
      end

      it "should raise when given a block" do
        lambda {
          @object.background.foo { }
        }.should raise_error(ArgumentError)
      end

      it "should handle missing methods" do
        @object.should_receive(:method_missing)
        @object.background.no_method
      end

      it "should pass through any options" do
        @queue.should_receive(:publish).with(anything,
                                             hash_including(:ttl => 1))
        @object.background(:ttl => 1).foo
      end

      it "should always use the :marshal encoding" do
        @queue.should_receive(:publish).with(anything,
                                             hash_including(:encoding => :marshal))
        @object.background.foo
      end
    end
  end

end
