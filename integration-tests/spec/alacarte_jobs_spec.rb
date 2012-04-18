require 'spec_helper'
require 'torquebox-messaging'

describe "long running jobs alacarte with a timeout" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/long-running-jobs-has-timeout
      env: development

    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..

    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should be interrupted" do
    queue = TorqueBox::Messaging::Queue.new('/queue/response')
    queue.receive(:timeout => 120_000).should == 'started'
    queue.receive(:timeout => 5_000).should be_nil
  end
end

describe "long running jobs alacarte with timeout of zero" do
  deploy <<-END.gsub(/^ {4}/, '')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/long-running-jobs-zero-timeout
      env: development

    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..

    ruby:
      version: #{RUBY_VERSION[0, 3]}
  END

  it "should not be interrupted" do
    queue = TorqueBox::Messaging::Queue.new('/queue/response')
    queue.receive(:timeout => 120_000).should == 'started'
    queue.receive(:timeout => 5_000).should == 'done'
  end
end

describe "long running jobs alacarte with no timeout specified" do
  deploy <<-END.gsub(/^ {4}/, '')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/long-running-jobs-no-timeout
      env: development

    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..

    ruby:
      version: #{RUBY_VERSION[0, 3]}
  END

  it "should not be interrupted" do
    queue = TorqueBox::Messaging::Queue.new('/queue/response')
    queue.receive(:timeout => 120_000).should == 'started'
    queue.receive(:timeout => 5_000).should == 'done'
  end
end