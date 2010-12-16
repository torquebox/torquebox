require 'spec_helper'
require 'org.torquebox.torquebox-messaging-client'

describe "messaging rack test" do
  deploy :path => "rails/2.x/messaging-rails.yml"

  it "should receive a ham biscuit" do
    visit "/messaging-rails/message/queue?text=ham+biscuit"
    result = TorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 2000)
    result.should == "result=ham biscuit"
  end

  it "should create queues and consumers dynamically" do
    visit "/messaging-rails/message/start?name=lanceball"
    puts "JC: visited start"
    TorqueBox::Messaging::Queue.new("/queues/lanceball").publish("nouns and verbs")
    puts "JC: published"
    result = TorqueBox::Messaging::Queue.new("/queues/results").receive(:timeout => 2000)
    puts "JC: received"
    result.should == "result=nouns and verbs"
    visit "/messaging-rails/message/stop"
  end

end
