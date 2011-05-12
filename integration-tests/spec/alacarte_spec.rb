require 'spec_helper'
require 'torquebox-messaging'

shared_examples_for "alacarte" do

  it "should detect file writing activity" do
    responseq = TorqueBox::Messaging::Queue.new( '/queue/response' )
    response = responseq.receive( :timeout => 120_000 )
    5.times do
      response.should == 'done'
      response = responseq.receive( :timeout => 2_000 )
    end
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
