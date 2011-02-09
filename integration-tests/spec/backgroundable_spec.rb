require 'spec_helper'
require 'org.torquebox.torquebox-messaging-client'

describe "backgroundable tests" do

  deploy :path => "rack/background-knob.yml"

  before(:each) do
    @foreground = TorqueBox::Messaging::Queue.new("/queues/foreground")
    @background = TorqueBox::Messaging::Queue.new("/queues/background")
  end

  it "should wait asynchronously" do
    visit "/background"
    page.should have_content('it worked')
    @background.publish "release"
    result = @foreground.receive(:timeout => 25000)
    result.should == "success"
  end

end
