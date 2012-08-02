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
end

describe "stateless jobs alacarte" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/jobs
      env: production

    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..

    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should not retain state after execution" do
    responseq = TorqueBox::Messaging::Queue.new( '/queue/stateless_response' )
    response = responseq.receive( :timeout => 120_000 )
    5.times do
      response.should == 'done'
      response = responseq.receive( :timeout => 120_000 )
    end
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
