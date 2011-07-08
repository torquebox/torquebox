require 'spec_helper'
require "#{File.dirname(__FILE__)}/../apps/rack/futures/something"
require "#{File.dirname(__FILE__)}/../apps/rack/futures/app/tasks/some_task"

remote_describe 'in container futures tests' do

  deploy <<-END.gsub(/^ {4}/,'')
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/futures
    ruby:
      version: #{RUBY_VERSION[0,3]}
    queues:
      /queue/backchannel:
  END

  shared_examples_for 'something with a future' do
    before(:each) do
      @backchannel = TorqueBox::Messaging::Queue.new( '/queue/backchannel' )
    end

    it "should work" do
      future = @something.foo
      @backchannel.receive( :timeout => 120_000 ).should == 'release'
      future.result( 10_000 ).should == 'bar'
      future.should be_started
      future.should be_complete
      future.should_not be_error
    end

    it "should raise the remote error" do
      future = @something.error
      @backchannel.receive( :timeout => 120_000 ).should == 'release'
      lambda { future.result }.should raise_error
      future.should be_started
      future.should_not be_complete
      future.should be_error
    end

    it "should set the status" do
      future = @something.with_status
      sleep(0.1) until future.started?
      future.status.should == '1'
      future.status.should == '2'
      @backchannel.publish( 'finish' )
    end
  end

  describe 'futures from backgroundable' do
    before(:each) do
      @something = Something.new
    end

    it_should_behave_like 'something with a future'
  end

  describe 'futures from /app/tasks' do
    before(:each) do
      @something = Object.new
      def @something.method_missing(meth)
        SomeTask.async(meth)
      end
    end

    it_should_behave_like 'something with a future'
  end

end
