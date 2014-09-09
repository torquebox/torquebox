require 'spec_helper'
require 'torquebox-messaging'

describe "messaging alacarte rack test" do

  embedded("main.rb", :dir => "#{apps_dir}/alacarte/messaging")

  it "should work" do
    tstamp = Time.now
    type = wildfly? ? :hornetq_wildfly : :hornetq_standalone
    port = wildfly? ? 8080 : 5445
    TorqueBox::Messaging::Connection.new(:host => 'localhost',
                                         :port => port,
                                         :remote_type => type) do |connection|
      queue = connection.queue('queue/simple_queue')
      queue.publish(:tstamp => tstamp, :cheese => "gouda")
      backchannel = connection.queue('queue/backchannel')
      release = backchannel.receive(:timeout => 120_000)
      release.should == 'release'
    end
  end
end
