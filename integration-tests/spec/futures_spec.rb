require 'spec_helper'

describe 'futures tests' do

  deploy <<-END.gsub(/^ {4}/,'')
    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/futures
    web:
      context: /futures
    ruby:
      version: #{RUBY_VERSION[0,3]}
    queues:
      /queue/backchannel:
        durable: false
  END

  shared_examples_for 'something with a future' do

    it "should work" do
      visit "/futures/should_work?task=@task"
      page.should have_content('it worked')
      page.find("#result").text.should == 'bar'
      status = page.find("#status")[:class]
      status.should =~ /started/
      status.should =~ /complete/
      status.should_not =~ /error/
    end

    it "should raise the remote error" do
      visit "/futures/should_raise_error?task=@task"
      page.should have_content('it worked')
      status = page.find("#status")[:class]
      status.should =~ /started/
      status.should_not =~ /complete/
      status.should =~ /error/
    end

    it "should set the status" do
      visit "/futures/should_set_status?task=@task"
      page.should have_content('it worked')
      ['1', '2', '3', '4'].should include(page.find("#interim_status").text)
      page.find("#result").text.should == 'ding'
      page.find("#final_status").text.should == '4'
    end
  end

  describe 'futures from backgroundable' do
    before(:each) do
      @task = false
    end

    it_should_behave_like 'something with a future'
  end

  describe 'futures from /app/tasks' do
    before(:each) do
      @task = true
    end

    it_should_behave_like 'something with a future'
  end

end
