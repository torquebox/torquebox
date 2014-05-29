require 'spec_helper'

java_import java.util.concurrent.CountDownLatch
java_import java.util.concurrent.TimeUnit

describe "Queue" do
  it "should apply any default options" do
    queue = TorqueBox::Messaging::Queue.new("def-opt", durable: false, default_options: {encoding: :json})
    listener = queue.respond(decode: false) { |m| m.content_type }
    queue.request("ham").should == "application/json"
    listener.close
  end

  describe 'request/respond' do
    it 'should work' do
      queue = TorqueBox::Messaging::Queue.new("req-resp", durable: false)
      listener = queue.respond do |m|
        m.upcase
      end
      queue.request("hi").should == "HI"
      listener.close
    end

    describe 'request' do
      it 'should take a block' do
        queue = TorqueBox::Messaging::Queue.new("req-resp", durable: false)
        listener = queue.respond do |m|
          m.upcase
        end
        queue.request("ham") do |m|
          m.should == 'HAM'
          m + ' biscuit'
        end.should == "HAM biscuit"
        listener.close
      end

      it 'should take a timeout & timeout_val' do
        queue = TorqueBox::Messaging::Queue.new("req-resp-timeout", durable: false)
        listener = queue.respond { |_|  sleep(0.5) }
        queue.request("ham", timeout: 1, timeout_val: :timeout).should == :timeout
      end

      it 'should take an encoding' do
        queue = TorqueBox::Messaging::Queue.new("req-resp", durable: false)
        listener = queue.respond(decode: false) { |m| m.content_type }
        queue.request("ham", encoding: :edn).should == "application/edn"
        listener.close
      end

      it 'should use the default encoding' do
        TorqueBox::Messaging.default_encoding = :edn
        begin
          queue = TorqueBox::Messaging::Queue.new("req-resp-default-encoding", durable: false)
          listener = queue.respond(decode: false) { |m| m.content_type }
          queue.request("ham").should == "application/edn"
          listener.close
        ensure
          TorqueBox::Messaging.default_encoding = :marshal
        end
      end
    end

    describe 'respond' do
      it "should allow the block to get the raw message" do
        queue = TorqueBox::Messaging::Queue.new("req-resp", durable: false)
        listener = queue.respond(decode: false) do |m|
          m.content_type
        end
        queue.request("ham") do |m|
          m.should == 'application/ruby-marshal'
          true
        end.should be_true
        listener.close
      end
    end
  end
end
