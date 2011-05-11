require 'spec_helper'
require 'torquebox-messaging'

describe "messaging rack test" do

  deploy "rack/messaging-knob.yml"

  it "should receive a ham biscuit" do
    visit "/messaging-rack/?ham-biscuit"
    result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 120_000)
    result.should == "result=ham-biscuit"
  end

end
