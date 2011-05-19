require 'spec_helper'
require 'torquebox-messaging'

describe "backgroundable simple gem tests" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/background-simple-gem
      env: development
    web:
      context: /background_simple_gem
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  before(:each) do
    @foreground = TorqueBox::Messaging::Queue.new("/queues/foreground")
    @background = TorqueBox::Messaging::Queue.new("/queues/background")
  end

  it "should wait asynchronously" do
    visit "/background_simple_gem"
    page.should have_content('it worked')
    @background.publish "release"
    result = @foreground.receive(:timeout => 25000)
    result.should == "success"
  end

  it "should properly handle backgrounded methods on reloaded classes" do
    visit "/background_simple_gem?redefine=1"
    page.should have_content('it worked')
    @background.publish "release"
    result = @foreground.receive(:timeout => 25000)
    result.should == "success"
  end
end
