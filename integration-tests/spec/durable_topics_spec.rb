require 'spec_helper'

remote_describe 'durable topics' do

  before(:each) do
    @topic = TorqueBox::Messaging::Topic.start "/topics/foo"
    @topic.connect_options[:client_id] = 'bacon'
  end

  after(:each) do
    @topic.stop
  end
  
  describe 'receive' do
    it "should be durable" do
      @topic.receive :durable => true, :timeout => 1
      @topic.publish 'biscuit'
      response = @topic.receive :durable => true, :timeout => 10_000
      response.should == 'biscuit'
    end
  end

  describe 'unsubscribe' do
    it "should work" do
      @topic.receive :durable => true, :timeout => 1
      @topic.publish 'biscuit'
      response = @topic.receive :durable => true, :timeout => 10_000
      response.should == 'biscuit'

      @topic.unsubscribe

      @topic.publish 'ham'
      response = @topic.receive :durable => true, :timeout => 10
      response.should be_nil

      @topic.publish 'gravy'
      response = @topic.receive :durable => true, :timeout => 10_000
      response.should == 'gravy'
    end
  end
end
