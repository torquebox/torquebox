require 'spec_helper'
require 'org.torquebox.torquebox-messaging-client'

describe "messaging rack test" do
  deploy :path => "rails/2.x/messaging-rails.yml"

  it "should receive a ham biscuit" do
    visit "/messaging-rails/message/queue?text=ham+biscuit"
    result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 2000)
    result.should eql("result=ham biscuit")
  end

end
