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
    result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 120_000)
    result.should == "result=ham-biscuit"
  end

  it "should receive a topic ham biscuit" do
    receive_thread = Thread.new {
      result = TorqueBox::Messaging::Topic.new('/topics/results').receive(:timeout => 30_000)
      result.should == "result=topic-ham-biscuit"
    }
    visit "/messaging-rack/?topic-ham-biscuit"
    receive_thread.join
  end

end
