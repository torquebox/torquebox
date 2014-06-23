require 'spec_helper'

java_import java.util.concurrent.CountDownLatch
java_import java.util.concurrent.TimeUnit

describe "Destination" do

  describe "publish/receive" do
    it "should work" do
      queue = TorqueBox::Messaging::Queue.new("pubrecv", durable: false)
      queue.publish("hi")
      queue.receive.should == "hi"
    end

    it "should work with a non-default encoding" do
      queue = TorqueBox::Messaging::Queue.new("pubrecv", durable: false)
      queue.publish([1,2], encoding: :edn)
      msg = queue.receive(decode: false)
      msg.content_type.should == "application/edn"
      msg.body.should == [1,2]
    end

    it "should have a default encoding of :marshal" do
      queue = TorqueBox::Messaging::Queue.new("pubrecv", durable: false)
      queue.publish([1,2])
      msg = queue.receive(decode: false)
      msg.content_type.should == "application/ruby-marshal"
    end

    it "should respect a change to the default encoding" do
      TorqueBox::Messaging.default_encoding = :edn
      begin
        queue = TorqueBox::Messaging::Queue.new("pubrecv", durable: false)
        queue.publish('foo')
        msg = queue.receive(decode: false)
        msg.content_type.should == "application/edn"
      ensure
        TorqueBox::Messaging.default_encoding = :marshal
      end
    end

    it "should throw when given invalid options" do
      queue = TorqueBox::Messaging::Queue.new("err", durable: false)
      lambda { queue.publish("hi", ham: 'biscuit') }.should raise_error(ArgumentError)
      lambda { queue.receive(ham: 'biscuit') }.should raise_error(ArgumentError)
      lambda { TorqueBox::Messaging::Queue.new("err", ham: :biscuit) }.should raise_error(ArgumentError)
    end

    describe 'receive' do
      it "should convert priority shortcuts" do
        queue = TorqueBox::Messaging::Queue.new("priority", durable: false)
        queue.publish(:low, priority: :low)
        queue.publish(:high, priority: :high)
        queue.receive.should == :high
        queue.receive.should == :low
      end

      it 'should set properties' do
        queue = TorqueBox::Messaging::Queue.new("props", durable: false)
        queue.publish('hi', properties: {'foo' => 'bar'})
        queue.receive(decode: false).properties['foo'].should == 'bar'
      end

      it 'should return :timeout_val on timeout' do
        queue = TorqueBox::Messaging::Queue.new("timeout", durable: false)
        queue.receive(timeout: 1, timeout_val: :success).should == :success
      end

      it 'should return an undecoded message when :decode is false' do
        queue = TorqueBox::Messaging::Queue.new("recv", durable: false)
        queue.publish('hi')
        queue.receive(decode: false).should be_a(org.projectodd.wunderboss.messaging.Message)
      end

      it 'should take a block' do
        queue = TorqueBox::Messaging::Queue.new("recv", durable: false)
        queue.publish('ahoyhoy')
        queue.receive do |m|
          m.should == 'ahoyhoy'
          m.upcase
        end.should == 'AHOYHOY'
      end
    end

  end

  describe 'listen' do
    it "should work" do
      queue = TorqueBox::Messaging::Queue.new("listen", durable: false)
      latch = CountDownLatch.new(1)
      listener = queue.listen do |m|
        latch.count_down
      end
      queue.publish('hi')
      latch.await(1, TimeUnit::SECONDS).should be true
      listener.close
    end

    it "should throw when given invalid options" do
      queue = TorqueBox::Messaging::Queue.new("err", durable: false)
      lambda { queue.listen(ham: 'biscuit') {|m| } }.should raise_error(ArgumentError)
    end

    it 'should pass the message when :decode is false' do
      queue = TorqueBox::Messaging::Queue.new("listen", durable: false)
      latch = CountDownLatch.new(1)
      listener = queue.listen(decode: false) do |m|
        m.should be_a(org.projectodd.wunderboss.messaging.Message)
        latch.count_down
      end
      queue.publish('hi')
      latch.await(1, TimeUnit::SECONDS).should be true
      listener.close
    end
  end
end
