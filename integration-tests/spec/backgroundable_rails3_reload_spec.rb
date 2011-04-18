require 'spec_helper'
require 'torquebox-messaging'

describe "rails 3 backgroundable tests" do
  mutable_app 'rails3/backgroundable_reload'
  deploy :path => "rails3/backgroundable_reload-knob.yml"

  before(:each) do
    @response = TorqueBox::Messaging::Queue.new( "/queues/response" )
  end

  it "should reload the model in the task runtime" do
    visit "/backgroundable_reload3/widgets"
    page.should have_content( 'it worked' )
    @response.receive(:timeout => 25000).should == 'response 0'
    @response.receive(:timeout => 25000).should == 'response 1'
    @response.receive(:timeout => 25000).should == 'response 2'
  end

end
