require 'torquebox/messaging/future'

include TorqueBox::Messaging

describe TorqueBox::Messaging::Future do
  
  before(:each) do
    @queue = mock( Queue )
    @response = nil
    @queue.stub( :receive ).and_return { @response }
    @future = Future.new( @queue )
  end

  describe "#started?" do
    it "should be false if the remote side hasn't started" do
      @future.should_not be_started
    end

    it "should be true if the remote side has started" do
      @response = { }
      @future.should be_started
    end

    it "should be true if an error occurs" do
      @response = { :error => 'foo' }
      @future.should be_started
    end

    it "should be true if an processing is complete" do
      @response = { :result => 'foo' }
      @future.should be_started
    end
  end

  describe "#complete?" do
    it "should be false if the remote side hasn't started" do
      @future.should_not be_complete
    end

    it "should be false if the remote side hasn't completed" do
      @response = { }
      @future.should_not be_complete
    end

    it "should be false if an error occurs" do
      @response = { :error => 'foo' }
      @future.should_not be_complete
    end

    it "should be true if an processing is complete" do
      @response = { :result => 'foo' }
      @future.should be_complete
    end
  end
  
  describe "#error?" do
    it "should be false if the remote side hasn't started" do
      @future.should_not be_error
    end

    it "should be false if the remote side hasn't completed" do
      @response = { }
      @future.should_not be_error
    end

    it "should be true if an error occurs" do
      @response = { :error => 'foo' }
      @future.should be_error
    end

    it "should be false if an processing is complete" do
      @response = { :result => 'foo' }
      @future.should_not be_error
    end
  end

  describe '#status' do
    it "should return nil if no status set" do
      @future.status.should be_nil
    end

    it "should return the remote status" do
      @response = { :status => 'biscuit' }
      @future.status.should == 'biscuit'
    end

    it "should remember the last status" do
      @response = { :status => 'biscuit' }
      @future.status.should == 'biscuit'
      @response = { }
      @future.status.should == 'biscuit'
    end
  end

  describe "#all_statuses" do
    it "should be an emtpy array if no status has been received" do
      @future.all_statuses.should == []
    end

    it "should be an have all the seen statuses" do
      @response = { :status => :ham }
      @future.status
      @response = { :status => :biscuit }
      @future.status
      @future.all_statuses.should == [:ham, :biscuit]
    end
  end
  
  describe "#status_changed?" do
    it "should return false if no status has been sent" do
      @future.should_not be_status_changed
    end

    it "should be true for the first status" do
      @response = { :status => :ham }
      @future.should be_status_changed
    end
    
    it "should return true if the status doesn't match the last" do
      @response = { :status => :ham }
      @future.status
      @future.should_not be_status_changed
      @response = { :status => :biscuit }
      @future.should be_status_changed
    end
  end
  
  describe "#result" do

    it "should raise if it fails to start before timeout" do
      lambda { @future.result( 1 ) }.should raise_error( TimeoutException )
    end

    it "should raise if it fails to complete before timeout" do
      @response = { }
      lambda { @future.result( 1 ) }.should raise_error( TimeoutException )
    end

    it "should raise if a remote error occurs" do
      @response = { :error => ArgumentError.new }
      lambda { @future.result( 1 ) }.should raise_error( ArgumentError )
    end

    it "should return the result if complete" do
      @response = { :result => :success! }
      @future.result.should == :success!
    end

  end

  describe "#method_missing" do

    it "should delegate to #result" do
      @result = mock(:result)
      @result.should_receive(:blah)
      @response = { :result => @result }
      @future.blah
    end
    
  end


end
