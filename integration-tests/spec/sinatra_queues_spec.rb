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
    visit "/uppercaser/up/toby+crawley"
    page.should have_content('TOBY CRAWLEY')
  end

  it "should be employed" do
    visit "/uppercaser/job"
    page.should have_content('employment!')
  end

end
