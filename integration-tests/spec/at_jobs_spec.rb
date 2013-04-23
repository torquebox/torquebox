require 'spec_helper'
require 'torquebox-messaging'
require 'torquebox-jobs'

remote_describe "at jobs" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/at-jobs
      env: development
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should deploy the :every, :until at job" do
    queue = TorqueBox::Messaging::Queue.new('/queue/response')

    # Every 440 ms for over 5 seconds, from now
    TorqueBox::Jobs::ScheduledJob.at('SimpleJob', :every => 440, :until => Time.now + 5)

    # First run of first spec takes the longest while job runtimes spin up
    count = queue.receive(:timeout => 15_000).nil? ? 0 : 1

    while (queue.receive(:timeout => 1_000))
      count += 1
    end

    # 5 seconds, every 440 ms
    count.should == 12

    TorqueBox::Jobs::ScheduledJob.remove_sync('SimpleJob').should == true
  end

  it "should deploy the :at, :every, :until at job" do
    queue = TorqueBox::Messaging::Queue.new('/queue/response')

    # Start in 1 second, then every 460 ms for over 4 seconds (5 seconds from now, but start is delayed)
    TorqueBox::Jobs::ScheduledJob.at('SimpleJob', :at => Time.now + 1, :every => 460, :until => Time.now + 5)

    # First run takes the longest while job installs
    count = queue.receive(:timeout => 5_000).nil? ? 0 : 1

    while (queue.receive(:timeout => 1_000))
      count += 1
    end

    # 4 seconds, every 460 ms
    count.should == 9

    TorqueBox::Jobs::ScheduledJob.remove_sync('SimpleJob').should == true
  end

  it "should deploy the :in, :repeat, :until at job" do
    queue = TorqueBox::Messaging::Queue.new('/queue/response')

    # Start in 1 second, then every 460 ms for over 4 seconds (5 seconds from now, but start is delayed)
    TorqueBox::Jobs::ScheduledJob.at('SimpleJob', :in => 1_000, :every => 460, :until => Time.now + 5)

    # First run takes the longest while job installs
    count = queue.receive(:timeout => 5_000).nil? ? 0 : 1

    while (queue.receive(:timeout => 1_000))
      count += 1
    end

    # 4 seconds, every 220 ms
    count.should == 9

    TorqueBox::Jobs::ScheduledJob.remove_sync('SimpleJob').should == true
  end

  it "should deploy the :in, :repeat, :every at job" do
    queue = TorqueBox::Messaging::Queue.new('/queue/response')

    # Start in 1 second, then repeat te job 10 times, every 150 ms
    TorqueBox::Jobs::ScheduledJob.at_sync('SimpleJob', :in => 1_000, :repeat => 10, :every => 150).should == true

    # First run takes the longest while job installs
    count = queue.receive(:timeout => 5_000).nil? ? 0 : 1

    while (queue.receive(:timeout => 1_000))
      count += 1
    end

    # 11, because the first execution is not counted
    count.should == 11

    TorqueBox::Jobs::ScheduledJob.remove_sync('SimpleJob').should == true
  end
end
