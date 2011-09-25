require 'spec_helper'

remote_describe "transactions testing" do
  require 'torquebox-messaging'

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/transactions
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  before(:each) do
    @input  = TorqueBox::Messaging::Queue.new('/queue/input')
    @output = TorqueBox::Messaging::Queue.new('/queue/output')
  end
    
  it "should not hang when receive times out" do
    response = nil
    thread = Thread.new {
      response = @output.receive(:timeout => 1)
    }
    @input.publish("anything")
    thread.join
    response.should be_nil
    @output.receive.should == 'yay!' # drain the queue for next test
  end

  it "should publish a message when no error occurs" do
    response = nil
    thread = Thread.new {
      response = @output.receive(:timeout => 10_000)
    }
    @input.publish("anything")
    thread.join
    response.should_not be_nil
  end

  it "should retry delivery when an error is tossed" do
    @input.publish("This message should trigger 5 retries")
    response = @output.receive(:timeout => 10_000)
    response.should match /success.*\s5\s/
  end

  it "should rollback published messages when an error is tossed" do
    response = nil
    thread = Thread.new {
      response = @output.receive(:timeout => 5_000)
    }
    @input.publish("This message should cause an error to be raised")
    thread.join
    response.should be_nil
  end

  it "should receive a message in the processor's transaction" do
    queue = TorqueBox::Messaging::Queue.start "/queue/foo"
    begin
      @input.publish("should receive from #{queue.name}")
      queue.publish("release")
      response = @output.receive(:timeout => 5_000)
      response.should == "got release"
    ensure
      queue.stop
    end
  end

  it "should rollback message when explicit transaction fails" do
    queue = TorqueBox::Messaging::Queue.start "/queue/foo"
    begin
      TorqueBox.transaction do
        queue.publish("whatevs")
        raise "rollback"
      end
      raise "should not get here"
    rescue Exception => e
      e.message.should == 'rollback'
      queue.receive(:timeout => 5_000).should be_nil
    ensure
      queue.stop
    end
  end

  it "should support nested messaging transactions" do
    queue = TorqueBox::Messaging::Queue.start "/queue/foo"
    begin
      TorqueBox.transaction do
        queue.publish("first")
        TorqueBox.transaction(:requires_new => false) do
          queue.publish("second")
        end
      end
      q = queue.to_a
      q[0].decode.should == 'first'
      q[1].decode.should == 'second'
      q.count.should == 2
    ensure
      queue.stop
    end
  end

  it "should support nested messaging transactions, the latter new" do
    queue = TorqueBox::Messaging::Queue.start "/queue/foo"
    begin
      TorqueBox.transaction do
        queue.publish("first")
        TorqueBox.transaction(:requires_new => true) do
          queue.publish("second")
        end
      end
      q = queue.to_a
      q[0].decode.should == 'second'
      q[1].decode.should == 'first'
      q.count.should == 2
    ensure
      queue.stop
    end
  end

  it "should rollback nested messaging transactions" do
    queue = TorqueBox::Messaging::Queue.start "/queue/foo"
    begin
      TorqueBox.transaction do
        queue.publish("first")
        TorqueBox.transaction(:requires_new => false) do
          queue.publish("second")
          raise "rollback"
        end
      end
      raise "should not get here"
    rescue Exception => e
      e.message.should == 'rollback'
      queue.count.should == 0
    ensure
      queue.stop
    end
  end

  it "should rollback nested messaging transactions, the latter new" do
    queue = TorqueBox::Messaging::Queue.start "/queue/foo"
    begin
      TorqueBox.transaction do
        queue.publish("first")
        begin
          TorqueBox.transaction(:requires_new => true) do
            queue.publish("second")
            raise "rollback"
          end
        rescue Exception => e
          e.message.should == 'rollback'
        end
      end
      queue.first.decode.should == 'first'
      queue.count.should == 1
    ensure
      queue.stop
    end
  end

  it "should not rollback non-transactional messages" do
    queue = TorqueBox::Messaging::Queue.start "/queue/foo"
    begin
      TorqueBox.transaction do
        queue.publish("first")
        queue.publish("second", :tx => false)
        raise "rollback"
      end
      raise "should not get here"
    rescue Exception => e
      e.message.should == 'rollback'
      queue.first.decode.should == 'second'
      queue.count.should == 1
    ensure
      queue.stop
    end
  end

end
