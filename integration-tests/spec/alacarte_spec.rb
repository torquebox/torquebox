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
      jobs.count.should == 4
      jobs.map { |j| j.name }.should =~ [ 'job.one', 'job.two', 'job.three', 'job.four' ]
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

    it "should not fail if a job is not found" do
      job = TorqueBox::ScheduledJob.lookup('job.aaa.abc')
      job.should == nil
    end

    it "should remove a job by name" do
      job = TorqueBox::ScheduledJob.lookup('job.one')
      job.name.should == 'job.one'
      TorqueBox::ScheduledJob.remove('job.one')
      TorqueBox::ScheduledJob.lookup('job.one').should == nil
    end

    it "should not fail with lookup of a stopped job" do
      job = TorqueBox::ScheduledJob.lookup('job.four')
      job.name.should == 'job.four'
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
    queue = TorqueBox::Messaging::Queue.new("/queue/runtime_response")

    TorqueBox::ScheduledJob.list.count.should == 0
    TorqueBox::ScheduledJob.schedule('SimpleJob', "*/1 * * * * ?", :config => {"queue" => queue.name}).should == true
    TorqueBox::ScheduledJob.list.count.should == 1

    5.times do
      msg = queue.receive(:timeout => 10_000)
      msg[:state].should == :running
      msg[:options].should == {"queue" => queue.name}
    end

    job = TorqueBox::ScheduledJob.lookup('SimpleJob')
    job.name.should == 'SimpleJob'
    job.is_singleton.should == true

    TorqueBox::ScheduledJob.remove('SimpleJob').should == true
    TorqueBox::ScheduledJob.list.count.should == 0
  end


  it "should deploy the job with different name" do
    TorqueBox::ScheduledJob.list.count.should == 0
    TorqueBox::ScheduledJob.schedule('SimpleJob', "*/10 * * * * ?", :name => "simple.job").should == true
    TorqueBox::ScheduledJob.list.count.should == 1

    job = TorqueBox::ScheduledJob.lookup('simple.job')
    job.name.should == 'simple.job'
    job.status.should == 'STARTED'

    TorqueBox::ScheduledJob.remove('simple.job').should == true
    TorqueBox::ScheduledJob.list.count.should == 0
  end

  it "should deploy the job with config" do
    TorqueBox::ScheduledJob.list.count.should == 0
    TorqueBox::ScheduledJob.schedule('SimpleJob', "*/10 * * * * ?", :name => "simple.config.job", :config => {:text => "text", :hash => {:a => 2}}).should == true
    TorqueBox::ScheduledJob.list.count.should == 1

    job = TorqueBox::ScheduledJob.lookup('simple.config.job')
    job.name.should == 'simple.config.job'
    job.status.should == 'STARTED'

    TorqueBox::ScheduledJob.remove('simple.config.job').should == true
    TorqueBox::ScheduledJob.list.count.should == 0
  end

  it "should replace a job" do
    TorqueBox::ScheduledJob.list.count.should == 0
    TorqueBox::ScheduledJob.schedule('SimpleJob', "*/10 * * * * ?", :description => "something").should == true
    TorqueBox::ScheduledJob.list.count.should == 1

    job = TorqueBox::ScheduledJob.lookup('SimpleJob')
    job.name.should == 'SimpleJob'
    job.description.should == 'something'

    TorqueBox::ScheduledJob.schedule('SimpleJob', "*/5 * * * * ?", :description => "new job").should == true
    TorqueBox::ScheduledJob.list.count.should == 1

    job = TorqueBox::ScheduledJob.lookup('SimpleJob')
    job.name.should == 'SimpleJob'
    job.description.should == 'new job'

    TorqueBox::ScheduledJob.remove('SimpleJob').should == true
    TorqueBox::ScheduledJob.list.count.should == 0
  end

  it "should not fail when the job class name includes module" do
    TorqueBox::ScheduledJob.schedule('SomeModule::AnotherSimpleJob', "*/5 * * * * ?").should == true

    TorqueBox::ScheduledJob.list.count.should == 1

    job = TorqueBox::ScheduledJob.lookup('SomeModule.AnotherSimpleJob')
    job.name.should == 'SomeModule.AnotherSimpleJob'

    TorqueBox::ScheduledJob.remove('SomeModule.AnotherSimpleJob').should == true

    TorqueBox::ScheduledJob.list.count.should == 0
  end

  it "should not replace a job when the class differ" do
    TorqueBox::ScheduledJob.list.count.should == 0
    TorqueBox::ScheduledJob.schedule('SimpleJob', "*/10 * * * * ?", :description => "something").should == true
    TorqueBox::ScheduledJob.list.count.should == 1

    job = TorqueBox::ScheduledJob.lookup('SimpleJob')
    job.name.should == 'SimpleJob'
    job.description.should == 'something'

    TorqueBox::ScheduledJob.schedule('SomeModule::AnotherSimpleJob', "*/5 * * * * ?", :description => "another something").should == true

    TorqueBox::ScheduledJob.list.count.should == 2

    job = TorqueBox::ScheduledJob.lookup('SomeModule.AnotherSimpleJob')
    job.name.should == 'SomeModule.AnotherSimpleJob'
    job.description.should == 'another something'

    TorqueBox::ScheduledJob.remove('SimpleJob').should == true
    TorqueBox::ScheduledJob.remove('SomeModule.AnotherSimpleJob').should == true

    TorqueBox::ScheduledJob.list.count.should == 0
  end

  it "should deploy the job stopped" do
    TorqueBox::ScheduledJob.list.count.should == 0
    TorqueBox::ScheduledJob.schedule('SimpleJob', "*/5 * * * * ?", :stopped => true).should == true
    TorqueBox::ScheduledJob.list.count.should == 1

    job = TorqueBox::ScheduledJob.lookup('SimpleJob')
    job.name.should == 'SimpleJob'
    job.status.should == 'STOPPED'
    job.start
    job.status.should == 'STARTED'

    TorqueBox::ScheduledJob.remove('SimpleJob').should == true
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
