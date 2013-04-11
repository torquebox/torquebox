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
    latch = java.util.concurrent.CountDownLatch.new(1)
    receive_thread = Thread.new {
      latch.count_down
      result = TorqueBox::Messaging::Topic.new('/topics/test').receive(:timeout => 30_000)
      result.should == "topic-ham-biscuit"
      result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 30_000)
      result.should == "TestTopicConsumer=topic-ham-biscuit"
    }
    latch.await(30, java.util.concurrent.TimeUnit::SECONDS)
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

  describe "remote message priorities" do
    it "should be fifo for same priority messages" do
      queue = TorqueBox::Messaging::Queue.new("/queues/remote")
      queue.publish "first"
      queue.publish "second"
      queue.publish "third"
      queue.publish "fourth"
      queue.receive.should == "first"
      queue.receive.should == "second"
      queue.receive.should == "third"
      queue.receive.should == "fourth"
    end
    it "should send higher priority messages first" do
      queue = TorqueBox::Messaging::Queue.new("/queues/remote")
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

  describe "message encoding" do
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

    def assert_encoding_equals(destination, encoding, destination_options, message_options)
      send("with_#{destination}", "/#{destination}/decode", destination_options) do |d|

        t = Thread.new do
          msg = d.receive(:timeout => 10_000, :decode => false)
          TorqueBox::Messaging::Message.new(msg).class::ENCODING.should == encoding
        end

        sleep(0.5)

        d.publish("something", message_options)

        t.join
      end
    end

    [:topic, :queue].each do |destination|

      it "should use the default TorqueBox message encoding for #{destination}" do
        assert_encoding_equals(destination, TorqueBox::Messaging::Message::DEFAULT_ENCODE_ENCODING, {}, {})
      end

      it "should use the message encoding provided when publishing a message into #{destination}" do
        assert_encoding_equals(destination, TorqueBox::Messaging::EdnMessage::ENCODING, {}, {:encoding => :edn})
      end

      it "should use the default message encoding set for selected #{destination}" do
        assert_encoding_equals(destination, TorqueBox::Messaging::EdnMessage::ENCODING, {:encoding => :edn}, {})
      end

      it "should use the message encoding provided when creating the message overriding the #{destination} destination default one" do
        assert_encoding_equals(destination, TorqueBox::Messaging::TextMessage::ENCODING, {:encoding => :edn}, {:encoding => :text})
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

  describe "queue management tasks" do
    it "should allow to pause and unpause the queue" do
      with_queue("/queues/management") do |queue|
        queue.paused?.should == false
        queue.pause
        queue.paused?.should == true
        queue.resume
        queue.paused?.should == false
      end
    end

    context "removing messages" do
      it "should remove all messages" do
        with_queue("/queues/management") do |queue|
          queue.publish("one")
          queue.publish("two")

          queue.count_messages.should == 2
          queue.remove_messages.should == 2
          queue.count_messages.should == 0
        end
      end

      it "should remove messages by filter" do
        with_queue("/queues/management") do |queue|
          queue.publish("tomatoe", :properties => { :type => 'vegetable' })
          queue.publish("chicken", :properties => { :type => 'meat' })

          queue.count_messages.should == 2
          queue.remove_messages("type = 'vegetable'").should == 1
          queue.count_messages.should == 1
          queue.receive.should == "chicken"
          queue.count_messages.should == 0
        end
      end

      it "should remove message by id" do
        with_queue("/queues/management") do |queue|
          queue.publish("tomatoe", :properties => { :type => 'vegetable' })
          queue.publish("chicken", :properties => { :type => 'meat' })

          ids = []

          queue.each { |message| ids << message.jms_message.jms_message_id }

          queue.count_messages.should == 2
          queue.remove_message(ids.first).should == true
          queue.count_messages.should == 1
          queue.receive.should == "chicken"
          queue.count_messages.should == 0
        end
      end
    end

    context "expiring messages" do
      before(:each) do
        @expire_queue = TorqueBox::Messaging::Queue.start("/queues/customexpire")
      end

      after(:each) do
        @expire_queue.stop if @expire_queue
      end

      it "should return the default expiry address" do
        with_queue("/queues/management") do |queue|
          queue.expiry_address.should == "jms.queue.ExpiryQueue"
        end
      end

      it "should change the expiry address" do
        with_queue("/queues/management") do |queue|
          queue.expiry_address = "jms.queue.#{@expire_queue.name}"
          queue.expiry_address.should == "jms.queue./queues/customexpire"
        end
      end

      it "should expire all messages" do
        with_queue("/queues/management") do |queue|
          queue.publish("one")
          queue.publish("two")

          queue.expiry_address = "jms.queue.#{@expire_queue.name}"

          queue.count_messages.should == 2
          queue.expire_messages.should == 2

          @expire_queue.count_messages.should == 2
        end
      end

      it "should expire messages by filter" do
        with_queue("/queues/management") do |queue|
          queue.publish("tomatoe", :properties => { :type => 'vegetable' })
          queue.publish("chicken", :properties => { :type => 'meat' })

          queue.expiry_address = "jms.queue.#{@expire_queue.name}"

          queue.count_messages.should == 2
          @expire_queue.count_messages.should == 0
          queue.expire_messages("type = 'vegetable'").should == 1
          queue.count_messages.should == 1
          @expire_queue.count_messages.should == 1
          @expire_queue.receive.should == "tomatoe"
          @expire_queue.count_messages.should == 0
          queue.receive.should == "chicken"
          queue.count_messages.should == 0
        end
      end

      it "should expire message by id" do
        with_queue("/queues/management") do |queue|
          queue.publish("tomatoe", :properties => { :type => 'vegetable' })
          queue.publish("chicken", :properties => { :type => 'meat' })

          queue.expiry_address = "jms.queue.#{@expire_queue.name}"

          ids = []

          queue.each { |message| ids << message.jms_message.jms_message_id }

          queue.count_messages.should == 2
          queue.expire_message(ids.first).should == true
          queue.count_messages.should == 1
          @expire_queue.count_messages.should == 1
          @expire_queue.receive.should == "tomatoe"
          @expire_queue.count_messages.should == 0
          queue.receive.should == "chicken"
          queue.count_messages.should == 0
        end
      end

      it "should report correct consumer count" do
        with_queue("/queues/management") do |queue|
          queue.consumer_count.should == 0

          t = Thread.new do
            queue.receive
          end

          sleep(0.5)

          queue.consumer_count.should == 1
          queue.publish("end")

          t.join
        end
      end

      it "should report correct scheduled messages count" do
        with_queue("/queues/management") do |queue|
          queue.scheduled_messages_count.should == 0
          queue.publish Time.now, :scheduled => Time.now + 2
          queue.scheduled_messages_count.should == 1
        end
      end
    end

    context "dead letter messages" do
      before(:each) do
        @dead_queue = TorqueBox::Messaging::Queue.start("/queues/customdead")
      end

      after(:each) do
        @dead_queue.stop if @dead_queue
      end

      it "should return the default dead letter address" do
        with_queue("/queues/management") do |queue|
          queue.dead_letter_address.should == "jms.queue.DLQ"
        end
      end

      it "should change the dead letter address" do
        with_queue("/queues/management") do |queue|
          queue.dead_letter_address = "jms.queue.#{@dead_queue.name}"
          queue.dead_letter_address.should == "jms.queue./queues/customdead"
        end
      end

      it "should send all messages to the dead letter address" do
        with_queue("/queues/management") do |queue|
          queue.publish("one")
          queue.publish("two")

          queue.dead_letter_address = "jms.queue.#{@dead_queue.name}"

          queue.count_messages.should == 2
          @dead_queue.count_messages.should == 0

          # Send all messages to dead queue
          queue.send_messages_to_dead_letter_address.should == 2

          queue.count_messages.should == 0
          @dead_queue.count_messages.should == 2
        end
      end
    end

    context "moving messages" do
      before(:each) do
        @second_queue = TorqueBox::Messaging::Queue.start("/queues/second")
      end

      after(:each) do
        @second_queue.stop if @second_queue
      end

      it "should move all messages" do
        with_queue("/queues/management") do |queue|
          queue.publish("one")
          queue.publish("two")

          queue.count_messages.should == 2
          @second_queue.count_messages.should == 0
          queue.move_messages(@second_queue.name)
          queue.count_messages.should == 0
          @second_queue.count_messages.should == 2
        end
      end

      it "should move messages by filter" do
        with_queue("/queues/management") do |queue|
          queue.publish("tomatoe", :properties => { :type => 'vegetable' })
          queue.publish("chicken", :properties => { :type => 'meat' })

          queue.count_messages.should == 2
          @second_queue.count_messages.should == 0
          queue.move_messages(@second_queue.name, "type = 'vegetable'")
          queue.count_messages.should == 1
          @second_queue.count_messages.should == 1

          @second_queue.receive.should == "tomatoe"
          @second_queue.count_messages.should == 0
          queue.receive.should == "chicken"
          queue.count_messages.should == 0
        end
      end
    end

  end
end

remote_describe "messaging processor tests" do
  deploy <<-END.gsub(/^ {4}/,'')
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/messaging
      env: production
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  context "message processors management" do
    describe "list" do
      it "should list all available message processors" do
        processors = TorqueBox::Messaging::MessageProcessor.list

        processors.size.should == 8

        processors.map { |p| p.name }.sort.should == [
            "/queue/simple_queue.SimpleProcessor",
            "/queue/stateless_queue.StatelessProcessor",
            "/queues/torquebox/messaging_processor_tests/tasks/torquebox_backgroundable.TorqueBox::Messaging::BackgroundableProcessor",
            "/queue/echo_queue.Torquebox::Messaging::EchoProcessor",
            "/queue/synchronous.SynchronousProcessor",
            "/queue/synchronous_with_selectors.SynchronousProcessor",
            "/queue/remotesync.SynchronousProcessor",
            "/queue/stopped.SimpleProcessor"
        ].sort
      end
    end

    describe "lookup" do
      it "should lookup a message processor" do
        processor = TorqueBox::Messaging::MessageProcessor.lookup("/queue/simple_queue", "SimpleProcessor")

        processor.should_not be_nil
        processor.name.should eql("/queue/simple_queue.SimpleProcessor")
        processor.concurrency.should == 1

        processor = TorqueBox::Messaging::MessageProcessor.lookup("/queue/echo_queue", "Torquebox::Messaging::EchoProcessor")

        processor.should_not be_nil
        processor.name.should eql("/queue/echo_queue.Torquebox::Messaging::EchoProcessor")
        processor.concurrency.should == 10

        processor = TorqueBox::Messaging::MessageProcessor.lookup("/queue/remotesync", "SynchronousProcessor")

        processor.should_not be_nil
        processor.name.should eql("/queue/remotesync.SynchronousProcessor")
        processor.concurrency.should == 2
        processor.synchronous?.should == true
      end

      it "should return nil if a message processor lookup fails" do
        processor = TorqueBox::Messaging::MessageProcessor.lookup("/queue/doesnotexists", "SomeProcessor")

        processor.should be_nil
      end
    end

    describe "update concurrency" do
      it "should update the concurrency (adding)" do
        processor = TorqueBox::Messaging::MessageProcessor.lookup("/queue/simple_queue", "SimpleProcessor")

        processor.should_not be_nil
        processor.name.should eql("/queue/simple_queue.SimpleProcessor")
        processor.concurrency.should == 1

        processor.concurrency = 5

        processor.concurrency.should == 5
      end

      it "should update the concurrency (removing)" do
        processor = TorqueBox::Messaging::MessageProcessor.lookup("/queue/echo_queue", "Torquebox::Messaging::EchoProcessor")

        processor.should_not be_nil
        processor.name.should eql("/queue/echo_queue.Torquebox::Messaging::EchoProcessor")
        processor.concurrency.should == 10

        processor.concurrency = 5

        processor.concurrency.should == 5
      end
    end
  end

  context "synchronous message processors" do
    before(:each) do
      @queue = TorqueBox::Messaging::Queue.new('/queue/synchronous')
      @queue_with_selectors = TorqueBox::Messaging::Queue.new('/queue/synchronous_with_selectors')
    end

    it "should reply to the message" do
      @queue.publish_and_receive("something", :timeout => 10000).should eql("Got something but I want bacon!")
    end

    it "should reply to the message when a selector is provided" do
      @queue_with_selectors.publish_and_receive("bike", :timeout => 10000, :properties => {"awesomeness" => 20}).should eql("Got bike but I want bacon!")
    end

    it "should timeout since the selector is not satisfied" do
      @queue_with_selectors.publish_and_receive("food", :timeout => 1000, :properties => {"awesomeness" => 5}).should be_nil
    end
  end

  context "remote destinations" do
    before(:each) do
      # Local "remote" queue
      @queue = TorqueBox::Messaging::Queue.new('/queue/remotesync')
    end

    it "should receive a message from remote queue" do
      @queue.publish_and_receive("kingdom", :timeout => 10000).should eql("Got kingdom but I want bacon!")
    end

  end

  context "exported destinations" do
    describe "exporting" do
      require 'torquebox-naming'

      it "should export a queue or topic" do
        TorqueBox::Naming.remote_context do |context|
          context.to_a.should_not be_empty
          context['queue/remotesync'].should_not be_nil
          context['topic/remotesync'].should_not be_nil
        end
      end
    end
  end

  context "stopped message processors" do
    before(:each) do
      @queue = TorqueBox::Messaging::Queue.new('/queue/stopped')
    end

    it "should not start the message processor after deployment" do
      processor = TorqueBox::Messaging::MessageProcessor.lookup("/queue/stopped", "SimpleProcessor")
      processor.started?.should == false
      @queue.consumer_count.should == 0
      processor.start
      @queue.consumer_count.should == 2
      processor.stop
    end
  end
end

def with_queue(name, opts = {})
  queue = TorqueBox::Messaging::Queue.start name, opts
  yield queue
ensure
  queue.stop if queue
end

def with_topic(name, opts = {})
  topic = TorqueBox::Messaging::Topic.start name, opts
  yield topic
ensure
  topic.stop if topic
end
