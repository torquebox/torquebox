require 'spec_helper'

describe "an app using a torquebox.rb" do

  deploy( { :application => { :root => "#{File.dirname(__FILE__)}/../apps/rack/basic-torquebox-rb" } } )

  context "external tests" do
    before(:each) do
      visit "/torquebox-rb" 
    end
    
    it "should be the correct environment" do
      page.find("#success")[:class].should =~ /env-ham/
    end

    it "should have the correct environment var" do
      page.find("#success")[:class].should =~ /gravy/
    end

    it "should have a pool with the proper settings" do
      pool = nil
      lambda { 
        pool = mbean('torquebox.pools:name=foo,app=an_app_using_a_torquebox_rb')
      }.should_not raise_error(javax.management.InstanceNotFoundException)
      pool.should_not be_nil
      pool.minimum_instances.should == 0
      pool.maximum_instances.should == 6
    end

    it "should have a queue we specify" do
      lambda { 
        mbean('org.hornetq:module=JMS,type=Queue,name="/queue/a-queue"')
      }.should_not raise_error(javax.management.InstanceNotFoundException)
    end

    it "should have a topic we specify" do
      lambda { 
        mbean('org.hornetq:module=JMS,type=Topic,name="/topic/a-topic"')
      }.should_not raise_error(javax.management.InstanceNotFoundException)
    end
    
    it "should not have a backgroundable queue (options_for w/a disable)" do
      lambda { 
        mbean('org.hornetq:module=JMS,type=Queue,name="/queues/torquebox/an_app_using_a_torquebox_rb/tasks/torquebox_backgroundable"')
      }.should raise_error(javax.management.InstanceNotFoundException)
    end

    it "should create a job" do
      job = mbean('torquebox.jobs:name=a_job,app=an_app_using_a_torquebox_rb')
      job.cron_expression.should == '*/1 * * * * ?'
      job.ruby_class_name.should == 'AJob'
    end

    it "should create a processor" do
      proc = mbean('torquebox.messaging.processors:name=/queue/another_queue/a_processor,app=an_app_using_a_torquebox_rb')
      proc.destination_name.should == '/queue/another-queue'
      proc.concurrency.should == 2
      proc.message_selector.should == "steak = 'salad'"
    end

    it "should create a service" do
      service = mbean('torquebox.services:name=a_service,app=an_app_using_a_torquebox_rb')
      service.ruby_class_name.should == 'AService'
    end

  end

  remote_describe "in container" do
    it "should have an authentication domain" do
      require 'torquebox-security'
      auth = TorqueBox::Authentication['ham']
      auth.should_not be_nil
    end

    it "should allow for multiple authentication domain" do
      require 'torquebox-security'
      auth = TorqueBox::Authentication['ham']
      auth.should_not be_nil
      auth = TorqueBox::Authentication['biscuit']
      auth.should_not be_nil
    end
  end
end
