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

  it "should receive a topic ham biscuit" do
    proc = mbean( 'torquebox.messaging.processors:name=/topics/test/test_topic_consumer,app=messaging_rack_test' )
    proc.client_id.should == 'the-client'
    proc.durable.should be_true
    proc.stop
    visit "/messaging-rack/?topic-ham-biscuit"
    proc.start
    result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 30_000)
    result.should == "TestTopicConsumer=topic-ham-biscuit"
  end

end

