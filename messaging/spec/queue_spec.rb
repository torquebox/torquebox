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

describe "Queue" do
  it "should apply any default options" do
    queue = TorqueBox::Messaging.queue("def-opt",
                                       :durable => false,
                                       :default_options => { :encoding => :json })
    listener = queue.respond(:decode => false) { |m| m.content_type }
    queue.request("ham", :timeout => 1_000, :timeout_val => :timeout).should == "application/json"
    listener.close
  end

  describe 'request/respond' do
    it 'should work' do
      queue = TorqueBox::Messaging.queue("req-resp", :durable => false)
      listener = queue.respond do |m|
        m.upcase
      end
      queue.request("hi", :timeout => 1_000, :timeout_val => :timeout).should == "HI"
      listener.close
    end

    describe 'request' do
      it 'should take a block' do
        queue = TorqueBox::Messaging.queue("req-resp", :durable => false)
        listener = queue.respond do |m|
          m.upcase
        end
        queue.request("ham", :timeout => 1_000, :timeout_val => :timeout) do |m|
          m.should == 'HAM'
          m + ' biscuit'
        end.should == "HAM biscuit"
        listener.close
      end

      it 'should take a timeout & timeout_val' do
        queue = TorqueBox::Messaging.queue("req-resp-timeout", :durable => false)
        listener = queue.respond { |_| sleep(0.5) }
        queue.request("ham", :timeout => 1, :timeout_val => :timeout).should == :timeout
        listener.close
      end

      it 'should take an encoding' do
        queue = TorqueBox::Messaging.queue("req-resp", :durable => false)
        listener = queue.respond(:decode => false) { |m| m.content_type }
        queue.request("ham", :encoding => :edn, :timeout => 1_000,
                      :timeout_val => :timeout).should == "application/edn"
        listener.close
      end

      it 'should use the default encoding' do
        TorqueBox::Messaging.default_encoding = :edn
        begin
          queue = TorqueBox::Messaging.queue("req-resp-default-encoding", :durable => false)
          listener = queue.respond(:decode => false) { |m| m.content_type }
          queue.request("ham", :timeout => 1_000, :timeout_val => :timeout).should == "application/edn"
          listener.close
        ensure
          TorqueBox::Messaging.default_encoding = :marshal
        end
      end
    end

    describe 'respond' do
      it "should allow the block to get the raw message" do
        queue = TorqueBox::Messaging.queue("req-resp", :durable => false)
        listener = queue.respond(:decode => false) do |m|
          m.content_type
        end
        queue.request("ham", :timeout => 1_000, :timeout_val => :timeout) do |m|
          m.should == 'application/ruby-marshal'
          true
        end.should be true
        listener.close
      end
    end
  end
end
