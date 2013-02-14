require 'spec_helper'

describe "rails3 injection test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/injection
      RAILS_ENV: development
    web:
      context: injection
    queues:
      /queues/injection_service:
        durable: false
      /queues/injection_job:
        durable: false
      /queues/injection_task:
        durable: false
      /queues/injection_enumerable:
        durable: false
    services:
      InjectionService:
    jobs:
      injection_job:
        job: InjectionJob
        cron: '* * * * * ?'
        description: Test injection from a job
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work for services defined in app/services" do
    visit "/injection/service"
    page.should have_content('it worked')
  end

  it "should work for jobs defined in app/jobs" do
    visit "/injection/job"
    page.should have_content('it worked')
  end

  it "should work for tasks defined in app/tasks" do
    visit "/injection/task"
    page.should have_content('it worked')
  end

  it "should work for predetermined injectables" do
    visit "/injection/predetermined"
    find( '#service-registry' ).text.should eql( "true" )
    find( '#service-target' ).text.should   eql( "true" )
  end

  it "should work for calls to __inject__" do
    visit "/injection/alt_inject"
    page.should have_content('it worked')
  end

  it "should work for Enumerables" do
    visit "/injection/enumerable"
    page.should have_content('it worked')
  end

end

describe "injection disabled in unknown directories" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/injection/unprocessed

    web:
      context: /injection-unprocessed

    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should not inject the queue by default in 'stuff' diretory" do
    visit "/injection-unprocessed"
    page.should have_content('it worked')
    find('#queue-injected').text.should eql("no")
  end
end

describe "injection enabled in custom directories" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/injection/custom_paths

    web:
      context: /injection-custom

    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should inject the queue 'stuff' diretory" do
    visit "/injection-custom"
    page.should have_content('it worked')
    find('#queue-injected').text.should eql("yes")
  end
end
