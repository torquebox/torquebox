require 'spec_helper'

describe "sinatra queues test" do

  deploy <<-END.gsub(/^ {4}/,'')
    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/queues
    web:
      context: /uppercaser
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should scream toby crawley" do
    visit "/uppercaser/up/toby%20crawley"
    page.should have_content('TOBY CRAWLEY')
  end

  remote_describe "jobs check" do
    include TorqueBox::Injectors
    it "should be employed" do
      msg = fetch('/queues/jobs').receive(:timeout => 45000)
      msg.should == "employment!"
    end
  end

end
