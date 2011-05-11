require 'spec_helper'
require 'torquebox-messaging'
require 'set'

describe "task concurrency" do

  deploy "rack/task-concurrency-knob.yml"

  shared_examples_for "concurrent processors" do
    before(:each) do
      @backchannel = TorqueBox::Messaging::Queue.new("/queues/backchannel")
    end

    it "should have concurrent processors" do
      path = "/task-concurrency?#{@type}" 
      visit path
      page.should have_content('tasks fired')
      responses = Set.new
      20.times do
        responses << @backchannel.receive(:timeout => 120_000)
      end
      responses.size.should > 1
    end
  end

  describe "backgroundable tasks" do
    before(:each) do
      @type = 'backgroundable'
    end
    it_should_behave_like "concurrent processors"
  end

  describe "/app/tasks tasks" do
    before(:each) do
      @type = 'app-tasks'
    end
    it_should_behave_like "concurrent processors"
  end

end
