require 'spec_helper'
require 'torquebox-messaging'

describe "messaging rails test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rails3.1/basic
    web:
      context: /messaging-rails31
    queues:
      /queue/hamalamb:
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  remote_describe "json encoding" do
    it "should send and receive" do
      queue = TorqueBox::Messaging::Queue.new( '/queue/hamalamb' )
      data = { 'a' => 'b' }
      queue.publish( data, :encoding => :json )
      queue.receive( :timeout => 120_000 ).should == data
    end
  end
end
