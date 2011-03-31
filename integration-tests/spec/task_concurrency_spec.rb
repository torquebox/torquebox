require 'spec_helper'
require 'torquebox-messaging'

describe "task concurrency" do

  deploy :path => "rack/task-concurrency-knob.yml"

  before(:each) do
    @backchannel = TorqueBox::Messaging::Queue.new("/queues/backchannel")
  end

  it "set concurreny for backgroundable tasks" do
    #warm up the queue and MPs
    visit "/task-concurrency?backgroundable"
    task_responses
    visit "/task-concurrency?backgroundable"
    task_responses
    
    visit "/task-concurrency?backgroundable"
    page.should have_content('tasks fired')
    responses = task_responses
    responses[0..1].should =~ %w{ bg1-sleep bg2-sleep }
    responses[2..3].should =~ %w{ bg1-awake bg2-awake }
  end

  it "set concurreny for app/tasks/ tasks" do
    #warm up the queue and MPs
    visit "/task-concurrency?app-tasks"
    task_responses
    visit "/task-concurrency?app-tasks"
    task_responses
    
    visit "/task-concurrency?app-tasks"
    page.should have_content('tasks fired')
    responses = task_responses
    responses[0..1].should =~ %w{ at1-sleep at2-sleep }
    responses[2..3].should =~ %w{ at1-awake at2-awake }
  end

  def task_responses
    response = []
    4.times do
      response << @backchannel.receive(:timeout => 25000)
    end
    response
  end
end
