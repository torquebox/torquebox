require 'spec_helper'
require 'torquebox-messaging'


describe "job concurrency" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/job-concurrency
      env: development

    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..

    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should allow more than three jobs to run at once" do
    queue = TorqueBox::Messaging::Queue.new('/queue/backchannel')
    results = []
    results << queue.receive(:timeout => 120_000)
    4.times do
      results << queue.receive(:timeout => 1_000)
    end

    expected = []
    5.times { |n| expected << "job-#{n}" }

    results.should =~ expected
  end
end
