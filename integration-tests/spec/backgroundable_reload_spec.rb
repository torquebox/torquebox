require 'spec_helper'
require 'torquebox-messaging'

shared_examples_for "rails backgroundable tests" do

  before(:each) do
    @response = TorqueBox::Messaging::Queue.new( "/queues/response" )
  end

  it "should reload the model in the task runtime" do
    pending("This no worky so well in a cluster", :if => TorqueSpec.domain_mode)
    visit "#{@context}/widgets"
    page.should have_content( 'it worked' )
    @response.receive(:timeout => 240_000).should == 'response 0'
    @response.receive(:timeout => 240_000).should == 'response 1'
    @response.receive(:timeout => 240_000).should == 'response 2'
  end

end

describe "rails 2 backgroundable tests" do
  mutable_app 'rails2/backgroundable_reload'
  it_should_behave_like "rails backgroundable tests"
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../target/apps/rails2/backgroundable_reload
      RAILS_ENV: development
    web:
      context: /backgroundable_reload
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  before(:each) do
    @model_path = File.join( MUTABLE_APP_BASE_PATH, 'rails2', 'backgroundable_reload', 'app', 'models', 'widget.rb' )
    @context = "/backgroundable_reload"
  end
end

describe "rails 3 backgroundable tests" do
  mutable_app 'rails3/backgroundable_reload'
  it_should_behave_like "rails backgroundable tests"
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../target/apps/rails3/backgroundable_reload
      RAILS_ENV: development
    web:
      context: /backgroundable_reload3
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  before(:each) do
    @model_path = File.join( MUTABLE_APP_BASE_PATH, 'rails3', 'backgroundable_reload', 'app', 'models', 'widget.rb' )
    @context = "/backgroundable_reload3"
  end
end
