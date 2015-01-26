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

describe "Topic" do
  describe 'subscribe' do
    it 'should work' do
      topic = TorqueBox::Messaging.topic("subscribe")
      responseq = TorqueBox::Messaging.queue("subscribe-responseq", :durable => false)
      subscribe = lambda { topic.subscribe('my-sub') { |m| responseq.publish(m.upcase) } }
      listener = subscribe.call
      topic.publish('hi')
      responseq.receive(:timeout => 1000).should == 'HI'
      listener.close
      topic.publish('hello')
      listener = subscribe.call
      responseq.receive(:timeout => 1000).should == 'HELLO'
      listener.close
    end
  end

  describe 'unsubscribe' do
    it 'should work' do
      topic = TorqueBox::Messaging.topic("subscribe")
      responseq = TorqueBox::Messaging.queue("subscribe-responseq", :durable => false)
      subscribe = lambda { topic.subscribe('my-sub') { |m| responseq.publish(m.upcase) } }
      listener = subscribe.call
      topic.publish('hi')
      responseq.receive(:timeout => 1000).should == 'HI'
      listener.close
      topic.unsubscribe('my-sub')
      topic.publish('failure')
      listener = subscribe.call
      responseq.receive(:timeout => 10, :timeout_val => :success).should == :success
      listener.close
    end
  end
end
