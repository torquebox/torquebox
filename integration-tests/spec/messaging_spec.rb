require 'spec_helper'
require 'torquebox-messaging'

describe "messaging rack test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/messaging
    web:
      context: /messaging-rack
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should receive a ham biscuit" do
    visit "/messaging-rack/?ham-biscuit"
    result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 30_000)
    result.should == "TestQueueConsumer=ham-biscuit"
  end

  it "should receive a topic ham biscuit" do
    receive_thread = Thread.new {
      result = TorqueBox::Messaging::Topic.new('/topics/test').receive(:timeout => 30_000)
      result.should == "topic-ham-biscuit"
      result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 30_000)
      result.should == "TestTopicConsumer=topic-ham-biscuit"
    }
    sleep(0.3) # ensure thread is blocking on receive from topic
    visit "/messaging-rack/?topic-ham-biscuit"
    receive_thread.join
  end

  context "message selectors" do
    before(:each) do
      @queue = TorqueBox::Messaging::Queue.new "/queues/selectors"
      visit "/messaging-rack/start?#{@queue.name}"
    end

    after(:each) do
      visit "/messaging-rack/stop?#{@queue.name}"
    end

    {
      'prop = true' => true,
      'prop <> false' => true,
      'prop = 5' => 5,
      'prop > 4' => 5,
      'prop = 5.5' => 5.5,
      'prop < 6' => 5.5,
      "prop = 'string'" => 'string'
    }.each do |selector, value|
      it "should be able to select with property set to #{value} using selector '#{selector}'" do
        @queue.publish value.to_s, :properties => { :prop => value }
        message = @queue.receive(:timeout => 1000, :selector => selector)
        message.should == value.to_s
      end
    end
    
  end
end


describe "browse" do
  before(:each) do
  end

  after(:each) do
  end

  it "should allow enumeration of the messages" do
    pending("Need to be able to start/stop externally first")
    queue = TorqueBox::Messaging::Queue.new "/queues/browseable"
    queue.start
    queue.publish "howdy"
    queue.first.text.should == 'howdy'
    queue.destroy
  end

  it "should accept a selector" do
    pending("Need to be able to start/stop externally first")
    queue = TorqueBox::Messaging::Queue.new "/queues/browseable", {}, :selector => 'blurple > 5'
    queue.start
    queue.publish "howdy", :properties => {:blurple => 5}
    queue.publish "ahoyhoy", :properties => {:blurple => 6}
    queue.first.text.should == 'ahoyhoy'
    queue.detect { |m| m.text == 'howdy' }.should be_nil
    queue.destroy
    
  end
end

