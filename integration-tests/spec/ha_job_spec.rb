require 'spec_helper'

require 'fileutils'
require 'torquebox-messaging'

describe "HA jobs test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/ha-jobs
      env: development

    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..

    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  describe "standalone" do
    it "should still work" do
      queue = TorqueBox::Messaging::Queue.new('/queues/node_name')
      condition = lambda { |message| message != nil }
      message = wait_for_condition(30, 1, condition) do
        queue.publish_and_receive('node_name', :timeout => 5000)
      end
      message.should_not be_nil
    end
  end

end

