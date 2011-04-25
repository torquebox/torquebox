require 'spec_helper'
require 'torquebox-messaging'

shared_examples_for "rails backgroundable tests" do

  before(:each) do
    @background = TorqueBox::Messaging::Queue.new( "/queues/background" )
  end

  it "should reload the model in the task runtime" do
    visit "#{@context}/widgets"
    page.should have_content( 'it worked' )
    @background.receive(:timeout => 25000).should == 'a response'

    rewrite_file( @model_path, 'a response', 'a new response' )
    @background.receive(:timeout => 25000).should == 'a new response'
  end

end

describe "rails 2 backgroundable tests" do
  mutable_app 'rails2/backgroundable_reload'
  deploy "rails2/backgroundable_reload-knob.yml"
  it_should_behave_like "rails backgroundable tests"

  before(:each) do
    @model_path = File.join( MUTABLE_APP_BASE_PATH, 'rails2', 'backgroundable_reload', 'app', 'models', 'widget.rb' )
    @context = "/backgroundable_reload"
  end
end

describe "rails 3 backgroundable tests" do
  mutable_app 'rails3/backgroundable_reload'
  deploy "rails3/backgroundable_reload-knob.yml"
  it_should_behave_like "rails backgroundable tests"

  before(:each) do
    @model_path = File.join( MUTABLE_APP_BASE_PATH, 'rails3', 'backgroundable_reload', 'app', 'models', 'widget.rb' )
    @context = "/backgroundable_reload3"
  end
end
