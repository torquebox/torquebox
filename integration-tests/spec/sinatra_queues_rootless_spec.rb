require 'spec_helper'

describe "sinatra queues rootless test" do

rootless = <<-ROOTLESS.gsub(/^ {4}/, '')
    queues:
      /queues/requests:
        durable: false
      /queues/responses:
        durable: false
      /queues/jobs:
        durable: false
ROOTLESS
  
rooted = <<-ROOTED.gsub(/^ {4}/, '')
    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/rootless
    jobs:
      publisher:
        job: JobQueuePublisher
        cron: '*/1 * * * * ?'
    messaging:
      /queues/requests: UpperCaser
    web:
      context: /uppercaser  
    ruby:
      version: #{RUBY_VERSION[0,3]}
ROOTED

  deploy rootless, rooted

  it "should scream toby crawley" do
    visit "/uppercaser/up/toby%20crawley"
    page.should have_content('TOBY CRAWLEY')
  end

  remote_describe "jobs check" do
    include TorqueBox::Injectors
    it "should be employed" do
      msg = inject('/queues/jobs').receive(:timeout => 45000)
      msg.should == "employment!"
    end
  end

end
