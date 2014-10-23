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


describe "Context" do

  it 'should work remotely' do
    # we have to trigger the local broker to start, and create a queue
    # there first
    TorqueBox::Messaging.queue('remote-conn', :durable => false)
    TorqueBox::Messaging::Context.new(:host => "localhost") do |c|
      q = c.queue('remote-conn')
      q.publish('hi')
      q.receive(:timeout => 1000).should == 'hi'
    end
  end

  it "should throw if a queue/topic is created on a non-remote context" do
    TorqueBox::Messaging::Context.new do |c|
      expect { c.queue("whatevs") }.to raise_error(/remote context/)
      expect { c.topic("whatevs") }.to raise_error(/remote context/)
    end
  end

  it "should throw if a listen is given a non-remote context" do
    q = TorqueBox::Messaging.queue('foobar', :durable => false)

    TorqueBox::Messaging::Context.new do |c|
      expect { q.listen(:context => c) { |_| } }.to raise_error(/remote context/)
    end
  end

  it "should throw if a respond is given a non-remote context" do
    q = TorqueBox::Messaging.queue('foobar', :durable => false)

    TorqueBox::Messaging::Context.new do |c|
      expect { q.respond(:context => c) { |_| } }.to raise_error(/remote context/)
    end
  end

end
