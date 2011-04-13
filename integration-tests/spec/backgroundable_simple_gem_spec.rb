require 'spec_helper'
require 'torquebox-messaging'

describe "backgroundable tests" do

  deploy "rack/background-simple-gem-knob.yml"

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

  it "should properly handle backgrounded methods on reloaded classes" do
    visit "/background?redefine=1"
    page.should have_content('it worked')
    @background.publish "release"
    result = @foreground.receive(:timeout => 25000)
    result.should == "success"
  end
end
