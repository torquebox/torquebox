require 'torquebox/messaging/future_responder'

include TorqueBox::Messaging

describe TorqueBox::Messaging::FutureResponder do
  def self.it_should_publish(message, priority=nil, &block)
    it "should publish with #{message.inspect}#{priority ? ' and priority :' + priority.to_s : ''}" do
      opts = { :correlation_id => 'ham-biscuit', :ttl => 1234, :encoding => :marshal }
      opts[:priority] = priority if priority
      queue = mock('queue')
      responder = FutureResponder.new(queue, 'ham-biscuit', 1234)
      queue.should_receive( :publish ).with( hash_including(message), hash_including( opts ) )
      block.call( responder )
    end
  end
    
  before(:each) do
    @queue = mock('queue')
    @queue.stub(:publish)
    @responder = FutureResponder.new(@queue, 'correlation-id')
  end

  describe '#started' do
    it_should_publish({}, :low) do |responder|
      responder.started
    end
  end

  describe '#status=' do
    it "should set the status" do
      @responder.status = 'biscuit'
      @responder.instance_variable_get('@status').should == 'biscuit'
    end

    it_should_publish({:status => 'biscuit'}, :normal) do |responder|
      responder.status = 'biscuit'
    end
  end

  describe '#result=' do
    it "should set the result" do
      @responder.result = 'biscuit'
      @responder.instance_variable_get('@result').should == 'biscuit'
    end

    it_should_publish({:result => 'biscuit'}, :high) do |responder|
      responder.result = 'biscuit'
    end

    it "should include the last status with the result" do
      queue = mock('queue')
      responder = FutureResponder.new(queue, 'ham-biscuit')
      queue.should_receive( :publish )
      responder.status = 'crepe'
      queue.should_receive( :publish ).with( hash_including({:result => 'biscuit', :status => 'crepe'}), anything )
      responder.result = 'biscuit'
    end
  end

  describe '#error=' do
    it "should set the error" do
      @responder.error = 'biscuit'
      @responder.instance_variable_get('@error').should == 'biscuit'
    end

    it_should_publish({:error => 'biscuit'}, :high) do |responder|
      responder.error = 'biscuit'
    end

    it "should include the last status with the error" do
      queue = mock('queue')
      responder = FutureResponder.new(queue, 'ham-biscuit')
      queue.should_receive( :publish )
      responder.status = 'crepe'
      queue.should_receive( :publish ).with( hash_including({:error => 'biscuit', :status => 'crepe'}), anything )
      responder.error = 'biscuit'
    end

  end

end
