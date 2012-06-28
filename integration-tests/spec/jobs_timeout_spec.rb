require 'spec_helper'
require 'torquebox-messaging'


describe "job timeout" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/job-timeout
      env: development

    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..

    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should interrupt the job with a real timeout" do
    queue = TorqueBox::Messaging::Queue.new('/queue/timeout-real')
    queue.receive(:timeout => 120_000).should == 'started'
    queue.receive(:timeout => 10_000, :selector => "completion = 'true'").should == 'interrupted'
  end

  it "should not interrupt the job with a 0s timeout" do
    queue = TorqueBox::Messaging::Queue.new('/queue/timeout0s')
    queue.receive(:timeout => 120_000).should == 'started'
    queue.receive(:timeout => 10_000, :selector => "completion = 'true'").should == 'done'
  end

  it "should not interrupt the job with no timeout" do
    queue = TorqueBox::Messaging::Queue.new('/queue/notimeout')
    queue.receive(:timeout => 120_000).should == 'started'
    queue.receive(:timeout => 10_000, :selector => "completion = 'true'").should == 'done'
  end

end
