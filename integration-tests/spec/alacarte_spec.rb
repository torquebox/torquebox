require 'spec_helper'
require 'torquebox-messaging'

shared_examples_for "alacarte" do

  it "should detect file writing activity" do
    responseq = TorqueBox::Messaging::Queue.new( '/queue/response' )
    response = responseq.receive( :timeout => 120_000 )
    5.times do
      response.should == 'done'
      response = responseq.receive( :timeout => 2_000 )
    end
  end

end

describe "jobs alacarte" do
  deploy "alacarte/jobs-knob.yml"
  it_should_behave_like "alacarte"
end

describe "services alacarte" do
  deploy "alacarte/services-knob.yml"
  it_should_behave_like "alacarte"
end
