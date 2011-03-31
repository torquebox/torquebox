require 'spec_helper'
require 'torquebox-messaging'

describe "rails 3 backgroundable tests" do
  mutable_app 'rails3/backgroundable_reload'
  deploy :path => "rails3/backgroundable_reload-knob.yml"

  before(:each) do
    @background = TorqueBox::Messaging::Queue.new( "/queues/background" )
    @model_path = File.join( MUTABLE_APP_BASE_PATH, 'rails3', 'backgroundable_reload', 'app', 'models', 'widget.rb' )
  end

  it "should reload the model in the task runtime" do
    visit "/backgroundable_reload3/widgets"
    page.should have_content( 'it worked' )
    @background.receive(:timeout => 25000).should == 'a response'

    rewrite_file( @model_path, 'a response', 'a new response' )
    @background.receive(:timeout => 25000).should == 'a new response'

    rewrite_file( @model_path, 'a new response', 'another new response' )
    @background.receive(:timeout => 25000).should == 'another new response'
  end

end
