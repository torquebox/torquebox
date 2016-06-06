require "spec_helper"
require "torquebox-messaging"

describe "messaging alacarte rack test" do

  embedded("main.rb", :dir => "#{apps_dir}/alacarte/messaging")

  it "should work for simple listeners" do
    tstamp = Time.now
    with_context do |context|
      queue = context.queue("queue/simple_queue")
      queue.publish(:tstamp => tstamp, :cheese => "gouda")
      backchannel = context.queue("queue/backchannel")
      release = backchannel.receive(:timeout => 120_000)
      release.should == "#{tstamp.to_f} // gouda"
    end
  end

  it "should work for synchronous responders" do
    with_context do |context|
      queue = context.queue("queue/synchronous_queue")
      queue.request("something").should == "Got something but I want bacon!"
    end
  end

  it "should work for synchronous responders with selectors" do
    with_context do |context|
      queue = context.queue("queue/synchronous_with_selectors")
      response = queue.request("bike", :properties => { "awesomeness" => 20 })
      response.should == "Got bike but I want bacon!"

      failed_response = queue.request("bike",
                                      :timeout => 1000,
                                      :properties => { "awesomeness" => 5 })
      failed_response.should be_nil
    end
  end

  def with_context(&block)
    type = wildfly? ? :hornetq_wildfly : :hornetq_standalone
    port = wildfly? ? 8080 : 5445
    username = wildfly? ? "testuser" : nil
    password = wildfly? ? "testuser1!" : nil
    TorqueBox::Messaging::Context.new(:host => "localhost",
                                      :port => port,
                                      :remote_type => type,
                                      :username => username,
                                      :password => password,
                                      &block)
  end
end
