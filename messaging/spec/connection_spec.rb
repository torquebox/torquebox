require 'spec_helper'


describe "Connection" do

  it 'should work remotely' do
    # we have to trigger the local broker to start, and create a queue
    # there first
    TorqueBox::Messaging::Queue.new('remote-conn', durable: false)
    TorqueBox::Messaging::Connection.new(host: "localhost") do |c|
      q = c.queue('remote-conn')
      q.publish('hi')
      q.receive(timeout: 1000).should == 'hi'
    end
  end

  it "should be able to create sessions" do
    TorqueBox::Messaging::Connection.new do |c|
      q = TorqueBox::Messaging::Queue.new("session", durable: false)
      c.create_session do |s|
        q.publish("hi", session: s)
      end
      q.receive(timeout: 1000).should == 'hi'
    end
  end

end
