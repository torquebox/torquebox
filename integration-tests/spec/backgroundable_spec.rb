require 'spec_helper'
require 'torquebox-messaging'

describe "backgroundable tests" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/background
      env: development
    web:
      context: /background
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  before(:each) do
    @foreground = TorqueBox::Messaging::Queue.new("queue/foreground")
    @background = TorqueBox::Messaging::Queue.new("queue/background")
  end

  it "should wait asynchronously" do
    visit "/background"
    page.should have_content('it worked')
    @background.publish "release"
    result = @foreground.receive(:timeout => 60000)
    result.should == "success"
  end

  it "should properly handle backgrounded methods on reloaded classes" do
    visit "/background?redefine=1"
    page.should have_content('it worked')
    @background.publish "release"
    result = @foreground.receive(:timeout => 60000)
    result.should == "success"
  end
end
