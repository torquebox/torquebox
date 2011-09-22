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
    pending("until we stop closing sessions before TB.tx commits")
    queue = TorqueBox::Messaging::Queue.start "/queue/foo"
    begin
      TorqueBox.transaction do
        queue.publish("whatevs")
        raise "should rollback"
      end
      raise "should not get here"
    rescue Exception => e
      raise "Wrong exception: #{$!}" unless e.message == 'should rollback'
      response = queue.receive(:timeout => 5_000)
      response.should be_nil
    ensure
      queue.stop
    end
  end

end
