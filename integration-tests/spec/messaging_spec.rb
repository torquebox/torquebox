require 'spec_helper'
require 'torquebox-messaging'

describe "messaging rack test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/messaging
    web:
      context: /messaging-rack

    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should receive a ham biscuit" do
    visit "/messaging-rack/?ham-biscuit"
    result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 30_000)
    result.should == "TestQueueConsumer=ham-biscuit"
  end

  it "should receive a topic ham biscuit" do
    receive_thread = Thread.new {
      result = TorqueBox::Messaging::Topic.new('/topics/test').receive(:timeout => 30_000)
      result.should == "topic-ham-biscuit"
      result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 30_000)
      result.should == "TestTopicConsumer=topic-ham-biscuit"
    }
    sleep(0.3) # ensure thread is blocking on receive from topic
    visit "/messaging-rack/?topic-ham-biscuit"
    receive_thread.join
  end

end

remote_describe "in-container messaging tests" do

  describe "message enumeration" do
    it "should allow enumeration of the messages" do
      with_queue("/queues/browseable") do |queue|
        queue.count.should == 0
        queue.publish "howdy"
        queue.first.decode.should == 'howdy'
        queue.count.should == 1
        TorqueBox::Messaging::Queue.new("/queues/browseable").count.should == 1
      end
    end

    it "should accept a selector" do
      with_queue("/queues/browseable") do |queue|
        queue.enumerable_options = { :selector => 'blurple > 5' }
        queue.publish "howdy", :properties => {:blurple => 5}
        queue.publish "ahoyhoy", :properties => {:blurple => 6}
        queue.first.decode.should == 'ahoyhoy'
        queue.detect { |m| m.decode == 'howdy' }.should be_nil
        queue.detect { |m| m.decode == 'ahoyhoy' }.should_not be_nil
      end
    end
  end

  context "message selectors" do
    {
      'prop = true' => true,
      'prop <> false' => true,
      'prop = 5' => 5,
      'prop > 4' => 5,
      'prop = 5.5' => 5.5,
      'prop < 6' => 5.5,
      "prop = 'string'" => 'string'
    }.each do |selector, value|
      it "should be able to select with property set to #{value} using selector '#{selector}'" do
        with_queue("/queues/selectors") do |queue|
          queue.publish value.to_s, :properties => { :prop => value }
          message = queue.receive(:timeout => 1000, :selector => selector)
          message.should == value.to_s
        end
      end
    end

  end


  describe "sending and receiving" do

    context "with an encoding of json" do
      it "should be able to publish to and receive from a queue" do
        with_queue("/queues/foo") do |queue|
          queue.publish ["howdy"], :encoding => :json
          message = queue.receive

          message.should eql( ["howdy"] )
        end
      end

      it "should be able to publish a complex type to and receive from a queue" do
        with_queue("/queues/foo") do |queue|
          data = { 'time' => Time.now, 'string' => 'abc' }
          queue.publish data, :encoding => :json
          message = queue.receive

          message.should eql( data )
        end
      end
    end

    [:marshal, :marshal_base64, nil].each do |encoding|

      context "with an encoding of #{encoding || 'default'}" do
        it "should be able to publish a string to and receive from a queue" do
          with_queue("/queues/foo") do |queue|
            queue.publish "howdy", :encoding => encoding
            message = queue.receive

            message.should eql( "howdy" )
          end
        end

        it "should be able to publish a complex type to and receive from a queue" do
          with_queue("/queues/foo") do |queue|
            data = { :time => Time.now, :string => 'abc' }
            queue.publish data, :encoding => encoding
            message = queue.receive

            message.should eql( data )
          end
        end


        it "should receive a binary file correctly" do
          with_queue("/queues/foo") do |queue|
            data = File.open("#{File.dirname(__FILE__)}/../src/test/resources/sample.pdf", "r") { |file| file.read }
            queue.publish data, :encoding => encoding
            message = queue.receive

            message.should eql( data )
          end
        end

        it "should publish to multiple topic consumers" do
          with_topic("/topics/foo") do |topic|
            threads, count = [], 10
            # Use a threadsafe "array"
            msgs = java.util.Collections.synchronizedList( [] )

            # Ensure all clients are blocking on the receipt of a message
            count.times { threads << Thread.new { msgs << topic.receive } }
            sleep(1)
            topic.publish "howdy", :encoding => encoding
            threads.each {|t| t.join}

            msgs.to_a.should eql( ["howdy"] * count )
          end
        end

        context "synchronous messaging" do
          it "should return value of block given to receive_and_publish" do
            with_queue("/queues/publish_and_receive") do |queue|
              response_thread = Thread.new {
                queue.receive_and_publish( :timeout => 10000, :encoding => encoding ) { |msg| msg.upcase }
              }
              message = queue.publish_and_receive "ping", :timeout => 10000, :encoding => encoding
              response_thread.join

              message.should eql( "PING" )
            end
          end

          it "should return request message if no block given" do
            with_queue("/queues/publish_and_receive") do |queue|
              response_thread = Thread.new {
                queue.receive_and_publish( :timeout => 10000, :encoding => encoding )
              }
              message = queue.publish_and_receive "ping", :timeout => 10000, :encoding => encoding
              response_thread.join

              message.should eql( "ping" )
            end
          end

          it "should not mess up with multiple consumers" do
            with_queue("/queues/publish_and_receive") do |queue|
              thread_count = 3
              response_threads = (1..thread_count).map do
                Thread.new {
                  queue.receive_and_publish( :timeout => 10000, :encoding => encoding ) { |msg| msg.upcase }
                }
              end

              message = queue.publish_and_receive "ping", :timeout => 10000, :encoding => encoding
              # Send extra messages to trigger all remaining response threads
              (thread_count - 1).times do
                queue.publish_and_receive "ping", :timeout => 10000, :encoding => encoding
              end
              response_threads.each { |thread| thread.join }

              message.should eql( "PING" )
            end
          end

          it "should allow a selector to be passed" do
            with_queue("/queues/publish_and_receive") do |queue|
              response_thread = Thread.new {
                queue.receive_and_publish( :timeout => 10000,
                                           :encoding => encoding,
                                           :selector => "age > 60 or tan = true" )
              }

              # Publish a non-synchronous message that should not match selector
              queue.publish( "young and tan", :encoding => encoding, :properties => { :age => 25, :tan => true } )
              # Publish a synchronous message that should not match selector
              queue.publish_and_receive( "young",
                                         :timeout => 25,
                                         :encoding => encoding,
                                         :properties => { :age => 25 } )
              # Publish a synchronous message that should match selector
              message = queue.publish_and_receive( "wrinkled",
                                                   :timeout => 10000,
                                                   :encoding => encoding,
                                                   :properties => { :age => 65, :tan => true } )
              message.should eql( "wrinkled" )
              response_thread.join

              # Drain any remaining messages off the queue
              2.times { queue.receive(:timeout => 10) }

            end
          end
        end
      end
    end

    context "destination not ready" do
      it "should block on publish until queue is ready" do
        queue = TorqueBox::Messaging::Queue.new "/queues/not_ready"
        # Start the queue in a separate thread after a delay
        setup_thread = Thread.new {
          sleep( 0.2 )
          TorqueBox::Messaging::Queue.start "/queues/not_ready"
        }
        # The queue will not be ready when we call the publish method
        queue.publish "something"
        message = queue.receive

        setup_thread.join
        queue.stop
        message.should eql( "something" )
      end

      it "should block on receive until topic is ready" do
        topic = TorqueBox::Messaging::Topic.new "/topics/not_ready"
        # Start the topic in a separate thread after a delay
        setup_thread = Thread.new {
          sleep( 0.1 )
          TorqueBox::Messaging::Topic.start "/topics/not_ready"
        }
        # The topic will not be ready when we call the receive method, and
        # if it's not ready before :timeout expires, an exception will be
        # tossed
        message = topic.receive :timeout => 500
        setup_thread.join
        topic.stop
        message.should be_nil
      end

      it "should block until startup_timeout reached" do
        queue = TorqueBox::Messaging::Queue.new "/queues/not_ready"
        lambda {
          queue.publish "something", :startup_timeout => 200
        }.should raise_error
      end
    end
  end

end

def with_queue(name)
  queue = TorqueBox::Messaging::Queue.start name
  yield queue
ensure
  queue.stop if queue
end

def with_topic(name)
  topic = TorqueBox::Messaging::Topic.start name
  yield topic
ensure
  topic.stop if topic
end
