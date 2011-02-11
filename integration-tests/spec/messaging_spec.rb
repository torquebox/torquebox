require 'spec_helper'
require 'org.torquebox.messaging-client'

describe "messaging rack test" do

  deploy :path => "rails2/messaging-knob.yml"

  it "should receive a ham biscuit" do
    visit "/messaging-rails/message/queue?text=ham+biscuit"
    result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 2000)
    result.should == "result=ham biscuit"
  end

end
