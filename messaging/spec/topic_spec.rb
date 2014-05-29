require 'spec_helper'

java_import java.util.concurrent.CountDownLatch
java_import java.util.concurrent.TimeUnit

describe "Topic" do
  describe 'subscribe' do
    it 'should work' do
      topic = TorqueBox::Messaging::Topic.new("subscribe")
      responseq = TorqueBox::Messaging::Queue.new("subscribe-responseq", durable: false)
      subscribe = lambda { topic.subscribe('my-sub') { |m| responseq.publish(m.upcase) } }
      listener = subscribe.call
      topic.publish('hi')
      responseq.receive(timeout: 1000).should == 'HI'
      listener.close
      topic.publish('hello')
      listener = subscribe.call
      responseq.receive(timeout: 1000).should == 'HELLO'
      listener.close
    end
  end

  describe 'unsubscribe' do
    it 'should work' do
      topic = TorqueBox::Messaging::Topic.new("subscribe")
      responseq = TorqueBox::Messaging::Queue.new("subscribe-responseq", durable: false)
      subscribe = lambda { topic.subscribe('my-sub') { |m| responseq.publish(m.upcase) } }
      listener = subscribe.call
      topic.publish('hi')
      responseq.receive(timeout: 1000).should == 'HI'
      listener.close
      topic.unsubscribe('my-sub')
      topic.publish('failure')
      listener = subscribe.call
      responseq.receive(timeout: 10, timeout_val: :success).should == :success
      listener.close
    end
  end
end
