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


describe "Connection" do

  it 'should work remotely' do
    # we have to trigger the local broker to start, and create a queue
    # there first
    TorqueBox::Messaging::Queue.new('remote-conn', durable: false)
    TorqueBox::Messaging::Connection.new(host: "localhost") do |c|
      q = c.queue('remote-conn')
      q.publish('hi')
      q.receive(timeout: 1000).should == 'hi'
    end
  end

  it "should be able to create sessions" do
    TorqueBox::Messaging::Connection.new do |c|
      q = TorqueBox::Messaging::Queue.new("session", durable: false)
      c.create_session do |s|
        q.publish("hi", session: s)
      end
      q.receive(timeout: 1000).should == 'hi'
    end
  end

end
