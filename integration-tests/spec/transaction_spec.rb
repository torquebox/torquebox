require 'spec_helper'

remote_describe "transactions testing" do
  require 'torquebox-core'
  include TorqueBox::Injectors

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/transactions
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should not hang when receive times out" do
    input = inject('/queue/input')
    output = inject('/queue/output')
    response = nil
    thread = Thread.new {
      response = output.receive(:timeout => 1)
    }
    input.publish("anything")
    thread.join
    response.should be_nil
    output.receive.should == 'yay!' # drain the queue for next test
  end

  it "should receive a message when no error occurs" do
    input = inject('/queue/input')
    output = inject('/queue/output')
    response = nil
    thread = Thread.new {
      response = output.receive(:timeout => 10_000)
    }
    input.publish("anything")
    thread.join
    response.should_not be_nil
  end

  it "should retry delivery when an error is tossed" do
    input = inject('/queue/input')
    output = inject('/queue/output')
    input.publish("This message should trigger 5 retries")
    response = output.receive(:timeout => 10_000)
    response.should match /success.*\s5\s/
  end

  it "should not receive a message when an error is tossed" do
    input = inject('/queue/input')
    output = inject('/queue/output')
    response = nil
    thread = Thread.new {
      response = output.receive(:timeout => 10_000)
    }
    input.publish("This message should cause an error to be raised")
    thread.join
    response.should be_nil
  end
end
