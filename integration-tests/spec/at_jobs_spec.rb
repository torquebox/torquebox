require 'spec_helper'
require 'torquebox-messaging'

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

    # Every 200 ms for over 5 seconds, from now
    TorqueBox::ScheduledJob.at('SimpleJob', :every => 220, :until => Time.now + 5)

    count = 0

    while (queue.receive(:timeout => 2_000))
      count += 1
    end

    # 5 seconds, every 220 ms
    count.should be_within(1).of(23)

    TorqueBox::ScheduledJob.remove('SimpleJob').should == true
  end

  it "should deploy the :at, :every, :until at job" do
    queue = TorqueBox::Messaging::Queue.new('/queue/response')

    # Start in 1 second, then every 220 ms for over 4 seconds (5 seconds from now, but start is delayed)
    TorqueBox::ScheduledJob.at('SimpleJob', :at => Time.now + 1, :every => 220, :until => Time.now + 5)

    count = 0

    while (queue.receive(:timeout => 2_000))
      count += 1
    end

    # 4 seconds, every 220 ms
    count.should be_within(1).of(19)

    TorqueBox::ScheduledJob.remove('SimpleJob').should == true
  end

  it "should deploy the :in, :repeat, :until at job" do
    queue = TorqueBox::Messaging::Queue.new('/queue/response')

    # Start in 1 second, then every 220 ms for over 4 seconds (5 seconds from now, but start is delayed)
    TorqueBox::ScheduledJob.at('SimpleJob', :in => 1_000, :every => 220, :until => Time.now + 5)

    count = 0

    while (queue.receive(:timeout => 2_000))
      count += 1
    end

    # 4 seconds, every 220 ms
    count.should be_within(1).of(19)

    TorqueBox::ScheduledJob.remove('SimpleJob').should == true
  end

  it "should deploy the :in, :repeat, :every at job" do
    queue = TorqueBox::Messaging::Queue.new('/queue/response')

    # Start in 1 second, then repeat te job 10 times, every 150 ms
    TorqueBox::ScheduledJob.at('SimpleJob', :in => 1_000, :repeat => 10, :every => 150)

    count = 0

    while (queue.receive(:timeout => 2_000))
      count += 1
    end

    # 11, because the first execution is not counted
    count.should be_within(1).of(11)

    TorqueBox::ScheduledJob.remove('SimpleJob').should == true
  end
end
