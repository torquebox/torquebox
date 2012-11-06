require 'spec_helper'
require 'torquebox-messaging'

describe "backgroundable tests" do

  app1 = <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/background
      env: development
    web:
      context: /background

    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

    app2 = <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/background
      env: development
    web:
      context: /background2
    tasks:
      Backgroundable:
        durable: false

    ruby:
      version: #{RUBY_VERSION[0,3]}
  END
  
  deploy app1, app2 

  before(:each) do
    @foreground = TorqueBox::Messaging::Queue.new("queue/foreground")
    @background = TorqueBox::Messaging::Queue.new("queue/background")
  end

  def url(params = { })
    "/background?" + params.merge(@additional_params || {}).map { |pair| pair.join('=') }.join('&')
  end
    

  shared_examples_for "backgrounded methods" do
    it "should wait asynchronously" do
      visit url
      page.should have_content('it worked')
      @background.publish "release"
      result = @foreground.receive(:timeout => 120000)
      result.should == "success"
    end

    it "should properly handle backgrounded methods on reloaded classes" do
      visit url(:redefine => 1)
      page.should have_content('it worked')
      @background.publish "release"
      result = @foreground.receive(:timeout => 120000)
      result.should == "success"
    end

    it "should not error on .background calls with newrelic installed" do
      visit url(:bar => 1)
      page.should have_content('it worked')
      @background.publish "release"
      result = @foreground.receive(:timeout => 120000)
      result.should == "success"
    end
  end

  context "instance methods" do
    it_should_behave_like "backgrounded methods"
  end


  context "class methods" do
    before(:all) do
      @additional_params = { :class_method => 1 }
    end
    
    it_should_behave_like "backgrounded methods"
  end

  context "durability" do

    def queue_mbean(app_name, &block)
      name = "jms.queue./queues/torquebox/#{app_name}/tasks/torquebox_backgroundable"
      mbean(%Q{org.hornetq:module=Core,type=Queue,address="#{name}",name="#{name}"}, &block) 
    end
    
    it "should be durable by default" do
      queue_mbean('backgroundable_tests_1') do |q|
        q.durable.should be_true
      end
    end

    it "should be configurable" do
      queue_mbean('backgroundable_tests_2') do |q|
        q.durable.should be_false
      end
    end
  end
end
