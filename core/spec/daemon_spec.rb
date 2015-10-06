# Copyright 2014 Red Hat, Inc, and individual contributors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require 'spec_helper'

java_import java.util.concurrent.CountDownLatch
java_import java.util.concurrent.TimeUnit

describe TorqueBox::Daemon do

  it "should start and run the action" do
    latch = CountDownLatch.new(1)
    d = TorqueBox::Daemon.new(:first!) do |daemon|
      latch.count_down
    end.start

    latch.await(10, TimeUnit::SECONDS).should == true
    d.stop
  end

  it "should start and run the action when extended" do
    class MyDaemon < TorqueBox::Daemon
      attr_reader :latch
      def initialize
        super(:extended_action)
        @latch = CountDownLatch.new(1)
      end

      def action
        latch.count_down
      end
    end

    d = MyDaemon.new.start

    d.latch.await(10, TimeUnit::SECONDS).should == true
    d.stop
  end

  it "should restart on error" do
    started = false
    restarted = CountDownLatch.new(1)
    TorqueBox::Daemon.new("restart") do |daemon|
      if started
        restarted.count_down
      else
        started = true
        raise Exception.new("BOOM")
      end
    end.start

    restarted.await(10, TimeUnit::SECONDS).should == true
  end

  it "should call a custom on_error" do
    latch = CountDownLatch.new(1)
    on_error = lambda do |daemon, err|
      err.message.should eq("(Exception) BOOM")
      latch.count_down
    end

    TorqueBox::Daemon.new("on_error", :on_error => on_error) do |daemon|
      raise Exception.new("BOOM")
    end.start

    latch.await(10, TimeUnit::SECONDS).should == true
  end

  it "should call an overridden on_error" do
    class MyDaemon < TorqueBox::Daemon
      attr_reader :latch
      def initialize
        super(:overridden_on_error)
        @latch = CountDownLatch.new(1)
      end

      def action
        raise Exception.new("BOOM")
      end

      def on_error(_)
        latch.count_down
      end
    end

    d = MyDaemon.new.start

    d.latch.await(10, TimeUnit::SECONDS).should == true
  end

  it "should call a custom on_stop" do
    latch = CountDownLatch.new(1)
    on_stop = lambda do |daemon|
      latch.count_down
    end

    TorqueBox::Daemon.new(:on_stop, :on_stop => on_stop) do |daemon|
    end.start.stop

    latch.await(10, TimeUnit::SECONDS).should == true
  end

  it "should not be running after run falls through" do
    latch = CountDownLatch.new(1)
    d = TorqueBox::Daemon.new(:fall_through) do |daemon|
      latch.count_down
    end.start

    latch.await(10, TimeUnit::SECONDS).should == true
    sleep(1)
    d.running?.should_not eq(true)
    d.started?.should eq(true)
  end

  it "should throw if you try to create the same named daemon more than once" do
    TorqueBox::Daemon.new(:blah)

    lambda do
      TorqueBox::Daemon.new(:blah)
    end.should raise_error(/blah already exists/)
  end
end
