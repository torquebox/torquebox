require 'spec_helper'
require 'torquebox-messaging'

shared_examples_for "alacarte" do

  it "should detect activity" do
    responseq = TorqueBox::Messaging::Queue.new( '/queue/response' )
    response = responseq.receive( :timeout => 120_000 )
    5.times do
      response.should == 'done'
      response = responseq.receive( :timeout => 120_000 )
    end
  end

  it "should have its init params" do
    responseq = TorqueBox::Messaging::Queue.new( '/queue/init_params' )
    response = responseq.receive( :timeout => 120_000 )
    
    response['color'].should == 'blue'
    response['an_array'].to_a.should == %w{ one two } 
  end
end

describe "jobs alacarte" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/jobs
      env: development
    
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END
    
  it_should_behave_like "alacarte"

  it "should not retain state after execution" do
    responseq = TorqueBox::Messaging::Queue.new( '/queue/stateless_response' )
    response = responseq.receive( :timeout => 120_000 )
    5.times do
      response.should == 'done'
      response = responseq.receive( :timeout => 120_000 )
    end
  end

  it "should call on_error if error" do
    errorq = TorqueBox::Messaging::Queue.new( '/queue/error' )
    error = errorq.receive( :timeout => 120_000 )
    5.times do
      error.should == 'an error'
      error = errorq.receive( :timeout => 120_000 )
    end
  end

  remote_describe "TorqueBox::ScheduledJob" do
    it "should list jobs" do
      jobs = TorqueBox::ScheduledJob.list
      jobs.count.should == 3
      jobs.map { |j| j.name }.should =~ [ 'job.one', 'job.two', 'job.three' ]
    end

    it "should lookup a job by name" do
      job = TorqueBox::ScheduledJob.lookup( 'job.one' )
      job.name.should == 'job.one'
      job.status.should == 'STARTED'
      job.stop
      job.status.should == 'STOPPED'
      job.start
      job.status.should == 'STARTED'
      job.should be_started
    end
  end
end

remote_describe "runtime jobs alacarte" do
  deploy <<-END.gsub(/^ {4}/, '')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/runtime_jobs
      env: development

    ruby:
      version: #{RUBY_VERSION[0, 3]}
  END

  it "should deploy the job" do
    TorqueBox::ScheduledJob.list.count.should == 0
    TorqueBox::ScheduledJob.schedule('SimpleJob', "*/10 * * * * ?")
    TorqueBox::ScheduledJob.list.count.should == 1

    job = TorqueBox::ScheduledJob.lookup('default')
    job.name.should == 'default'
    job.status.should == 'STARTED'
  end

  it "should stop the job" do
    job = TorqueBox::ScheduledJob.lookup('default')
    job.name.should == 'default'
    job.status.should == 'STARTED'
    job.stop
    job.status.should == 'STOPPED'
    job.start
    job.status.should == 'STARTED'
    job.stop
  end

  it "should deploy the job with different name" do
    TorqueBox::ScheduledJob.list.count.should == 1
    TorqueBox::ScheduledJob.schedule('SimpleJob', "*/10 * * * * ?", :name => "simple.job")
    TorqueBox::ScheduledJob.list.count.should == 2

    job = TorqueBox::ScheduledJob.lookup('simple.job')
    job.name.should == 'simple.job'
    job.status.should == 'STARTED'
  end

  it "should deploy the job with config" do
    TorqueBox::ScheduledJob.list.count.should == 2
    TorqueBox::ScheduledJob.schedule('SimpleJob', "*/10 * * * * ?", :name => "simple.config.job", :config => {:text => "text", :hash => {:a => 2}})
    TorqueBox::ScheduledJob.list.count.should == 3

    job = TorqueBox::ScheduledJob.lookup('simple.config.job')
    job.name.should == 'simple.config.job'
    job.status.should == 'STARTED'
  end


end

describe "modular jobs alacarte" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/modular_jobs
      env: development

    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..

    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it_should_behave_like "alacarte"
end

describe "modular jobs alacarte with torquebox.rb" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/modular_jobs_rb
      env: development

    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..

    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it_should_behave_like "alacarte"
end

describe "services alacarte" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/services
      env: development
    
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it_should_behave_like "alacarte"

  remote_describe "TorqueBox::Service" do
    it "should list services" do
      services = TorqueBox::Service.list
      services.count.should == 2
      services.map { |s| s.name }.should =~ [ 'SimpleService', 'TorqueSpec::Daemon' ]
    end

    it "should lookup a service by name" do
      service = TorqueBox::Service.lookup( 'SimpleService' )
      service.name.should == 'SimpleService'
      service.status.should == 'STARTED'
      service.stop
      service.status.should == 'STOPPED'
      service.start
      service.status.should == 'STARTED'
      service.should be_started
    end
  end
end

describe "services alacarte with gemfile" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/services-with-gemfile
      env: development

    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should not break when using a Gemfile" do
    # good 'nuf!
  end
end
