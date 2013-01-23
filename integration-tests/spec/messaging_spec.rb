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
    ensure_no_xa_error
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
    ensure_no_xa_error
  end

  it "should work with a processor that doesn't inherit from MessageProcessor" do
    visit "/messaging-rack/?parentless-ham-biscuit"
    result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 30_000)
    result.should == "ParentlessQueueConsumer=parentless-ham-biscuit"
    ensure_no_xa_error
  end

  def ensure_no_xa_error
    log_file = File.new(File.join(jboss_log_dir, 'server.log'))
    log_file.seek(-4096, IO::SEEK_END)
    log_file.read.should_not include('Invalid transaction state')
    log_file.close
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

  describe "message priorities" do
    it "should be fifo for same priority messages" do
      with_queue("/queues/priorities") do |queue|
        queue.publish "first"
        queue.publish "second"
        queue.publish "third"
        queue.publish "fourth"
        queue.receive.should == "first"
        queue.receive.should == "second"
        queue.receive.should == "third"
        queue.receive.should == "fourth"
      end
    end
    it "should send higher priority messages first" do
      with_queue("/queues/priorities") do |queue|
        queue.publish "first", :priority => :low
        queue.publish "second", :priority => :normal
        queue.publish "third", :priority => :high
        queue.publish "fourth", :priority => :critical
        queue.receive.should == "fourth"
        queue.receive.should == "third"
        queue.receive.should == "second"
        queue.receive.should == "first"
      end
    end
  end

  describe "message ttl" do
    it "should live" do
      with_queue("/queues/ttl") do |queue|
        queue.publish "live!", :ttl => 9999
        queue.receive(:timeout => 1000).should_not be_nil
      end
    end
    it "should die" do
      with_queue("/queues/ttl") do |queue|
        queue.publish "die!", :ttl => 1
        queue.receive(:timeout => 1000).should be_nil
      end
    end
  end

  describe "decode" do
    [:marshal, :marshal_base64, :edn, :json, :text].each do |encoding|
      it "should be callable more than once for the #{encoding} encoding" do
        with_queue("/queues/decode") do |q|
          value = encoding == :text ? "foo" : ["foo"]
          q.publish(value, :encoding => encoding)
          msg = q.receive(:timeout => 10_000, :decode => false)
          msg.should_not be_nil
          msg = TorqueBox::Messaging::Message.new(msg)
          msg.decode.should == value
          msg.decode.should == value
        end
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

  describe "receiving by yielding blocks" do

    it "should receive result of yielded block" do
      with_queue("/queue/foo") do |queue|
        queue.publish "success"
        queue.receive {|m| m + "ful"}.should == "successful"
      end
    end

    it "should rollback delivery if block raises exception" do
      with_queue("/queue/foo") do |queue|
        queue.publish "success"
        begin
          queue.receive {|m| raise "rollback" }
          raise "should not get here"
        rescue Exception => e
          e.message.should == "rollback"
        end
        queue.receive(:timeout => 500).should == "success"
      end
    end

    it "should not yield if receive times out" do
      with_queue("/queue/foo") do |queue|
        queue.receive(:timeout => 1) { |m| raise "fail" }.should == nil
      end
    end

  end

  describe "sending and receiving" do

    [:edn, :json].each do |encoding|

      context "with an encoding of #{encoding}" do
        it "should be able to publish to and receive from a queue" do
          with_queue("/queues/foo") do |queue|
            queue.publish ["howdy"], :encoding => encoding
            message = queue.receive

            message.should eql( ["howdy"] )
          end
        end

        it "should be able to publish a hash to and receive from a queue" do
          with_queue("/queues/foo") do |queue|
            data = { :array => [1, 'abc'], :int => 123, :string => 'abc' }
            queue.publish data, :encoding => encoding
            message = queue.receive

            message.should eql( data )
          end
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

          if encoding
            it "receive_and_publish should use the same encoding it was given" do
              with_queue("/queues/publish_and_receive") do |queue|
                response_thread = Thread.new {
                  queue.receive_and_publish( :timeout => 10000 ) { |msg| msg.upcase }
                }
                message = queue.publish_and_receive( "ping", :timeout => 10000,
                                                     :encoding => encoding, :decode => false)
                response_thread.join
                
                TorqueBox::Messaging::Message.
                  extract_encoding_from_message( message ).to_sym.should == encoding
                TorqueBox::Messaging::Message.new( message ).
                  decode.should eql( "PING" )
              end
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

    context "scheduled messages" do

      it "should successfully send a scheduled message to the queue" do
        with_queue("/queues/scheduled") do |queue|

          # Schedule a message for 2 seconds
          # Timeout after 10 seconds
          t = Thread.new do
            queue.receive(:timeout => 10000) do |start_time|
              start_time.should_not be_nil
              
              duration = (Time.now - start_time) * 1000.0
              duration.should be_within(200.0).of(2000.0)
            end
          end

          queue.publish Time.now, :scheduled => Time.now + 2

          t.join
        end
      end

      it "should successfully send a scheduled message to the topic" do
        # Allow to use fancy time in tests
        require 'active_support/core_ext/numeric/time'

        with_topic("/topics/scheduled", :client_id => 'scheduled-topic') do |topic|

          topic.receive(:durable => true, :timeout => 1)
          
          # Schedule a message for 2 seconds
          # Timeout after 10 seconds
          t = Thread.new do
            topic.receive(:durable => true, :timeout => 10_000) do |start_time|
              start_time.should_not be_nil
            
              duration = (Time.now - start_time) * 1000.0
              duration.should be_within(200.0).of(2000.0)
            end
          end

          topic.publish Time.now, :scheduled => 2.seconds.from_now

          t.join
        end
      end

      it "should not deliver the message before timeout" do
        with_queue("/queues/scheduled") do |queue|

          start_time = Time.now

          # Schedule a message for 5 seconds
          # Timeout after 1 second
          queue.publish_and_receive "wassup", :timeout => 1000, :scheduled => Time.now + 5
          duration = (Time.now - start_time) * 1000.0

          duration.should be_within(200.0).of(1000.0)
        end
      end
    end

    context "management" do
      before(:each) do
        @queue1 = TorqueBox::Messaging::Queue.start('/queues/1')
        @queue2 = TorqueBox::Messaging::Queue.start('/queues/2')
        @topic1 = TorqueBox::Messaging::Topic.start('/topics/1')
        @topic2 = TorqueBox::Messaging::Topic.start('/topics/2')
      end

      after(:each) do
        @queue1.stop
        @queue2.stop
        @topic1.stop
        @topic2.stop
      end

      it "should list queues" do
        queue_names = TorqueBox::Messaging::Queue.list.map(&:name)
        queue_names.should include("/queues/1")
        queue_names.should include("/queues/2")
        queue_names.should_not include("/topics/1")
      end

      it "should lookup a queue by name" do
        queue = TorqueBox::Messaging::Queue.lookup("/queues/1")
        queue.should_not be_nil
        queue.name.should == "/queues/1"
      end

      it "should list topics" do
        topic_names = TorqueBox::Messaging::Topic.list.map(&:name)
        topic_names.should include("/topics/1")
        topic_names.should include("/topics/2")
        topic_names.should_not include("/queues/1")
      end

      it "should lookup a topic by name" do
        topic = TorqueBox::Messaging::Topic.lookup("/topics/1")
        topic.should_not be_nil
        topic.name.should == "/topics/1"
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

def with_topic(name, opts = {})
  TorqueBox::Messaging::Topic.start name
  topic = TorqueBox::Messaging::Topic.new name, opts
  yield topic
ensure
  topic.stop if topic
end
