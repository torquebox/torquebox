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

describe 'AddressSettings' do
  before(:each) do
    # create a queue to start HQ
    @queue = random_queue
    @opts = TorqueBox::Messaging::HornetQ::AddressSettings.new(@queue)
  end

  it "should properly convert the match argument" do
    {
      "#"                   => "#",
      "jms.queue.#"         => "jms.queue.#",
      "jms.queue.foo.queue" => TorqueBox::Messaging.queue("foo.queue"),
      "jms.queue.foo-bar"   =>  TorqueBox::Messaging.queue("foo-bar")
    }.each do |expected,given|
      @opts.send(:normalize_destination_match, given).should == expected
    end

    ["jms.#", "*", "foo"].each do |match|
      lambda do
        @opts.send(:normalize_destination_match, match)
      end.should raise_error(ArgumentError)
    end
  end

  it "should allow setting everything" do
    lambda do
      @opts.address_full_message_policy = :drop
      @opts.dead_letter_address = @queue
      @opts.expiry_address = @queue
      @opts.last_value_queue = true
      @opts.send_to_dla_on_no_route = true
      @opts.expiry_delay = 1
      @opts.max_delivery_attempts = 1
      @opts.max_redelivery_delay = 10_000
      @opts.max_size_bytes = 1024
      @opts.page_cache_max_size = 1
      @opts.page_size_bytes = 1
      @opts.redelivery_delay = 1
      @opts.redelivery_multiplier = 2.0
      @opts.redistribution_delay = 1
    end.should_not raise_error

  end

  it "should set address_full_policy for last_value_queue" do
    @opts.should_receive(:address_full_message_policy=).with(:drop)
    @opts.last_value_queue = true
  end

  it "should take a destination or name for dlq" do
    lambda do
      @opts.dead_letter_address = @queue
      @opts.dead_letter_address = "jms.queue.foo"
    end.should_not raise_error
  end

  it "should take a destination or name for expiry" do
    lambda do
      @opts.expiry_address = @queue
      @opts.expiry_address = "jms.queue.foo"
    end.should_not raise_error
  end

  it "should actually apply the settings" do
    queue = random_queue
    dlq = random_queue
    delivery_count = 0
    listener = queue.listen do |m|
      delivery_count += 1
      fail Exception.new("EXPECTED EXCEPTION")
    end

    begin
      opts = TorqueBox::Messaging::HornetQ::AddressSettings.new(queue)
      opts.dead_letter_address = dlq
      opts.max_delivery_attempts = 2
      queue.publish(:boomer)
      dlq.receive(timeout: 1_000, timeout_val: :timeout).should == :boomer
      delivery_count.should == 2
    ensure
      listener.close
    end
  end

  describe 'destination_controller' do

    before(:all) do
      @queue = random_queue
      @topic = random_topic
    end

    it "should return a JMSQueueControl for a Queue and type :jms" do
      ctl = TorqueBox::Messaging::HornetQ.destination_controller(@queue)
      ctl.class.ancestors.should include(org.hornetq.api.jms.management.JMSQueueControl)
    end

    it "should return a QueueControl for a Queue and type :core" do
      ctl = TorqueBox::Messaging::HornetQ.destination_controller(@queue, :core)
      ctl.class.ancestors.should include(org.hornetq.api.core.management.QueueControl)
    end

    it "should return a TopicControl for a Topic, regardless of type" do
      ctl = TorqueBox::Messaging::HornetQ.destination_controller(@topic)
      ctl.class.ancestors.should include(org.hornetq.api.jms.management.TopicControl)
    end

  end
end
