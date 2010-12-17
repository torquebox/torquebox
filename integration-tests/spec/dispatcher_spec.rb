require 'org.torquebox.torquebox-messaging-client'
require 'org.torquebox.torquebox-messaging-container'

QUEUE_QUESTIONS = TorqueBox::Messaging::Queue.new("/queues/rspec/questions")
QUEUE_ANSWERS   = TorqueBox::Messaging::Queue.new("/queues/rspec/answers")

class Upcaser < TorqueBox::Messaging::MessageProcessor
  def on_message(body)
    QUEUE_ANSWERS.publish( body.upcase )
  end
end

describe "dispatcher test" do

  before(:each) do
    QUEUE_QUESTIONS.start
    QUEUE_ANSWERS.start
  end

  after(:each) do
    QUEUE_QUESTIONS.destroy
    QUEUE_ANSWERS.destroy
  end
  
  it "should create dynamic queues and consumers" do
    dispatcher = TorqueBox::Messaging::Dispatcher.new do
      map Upcaser, QUEUE_QUESTIONS
    end
    # dispatcher.start
    # QUEUE_QUESTIONS.publish "lkjsdf"
    # QUEUE_ANSWERS.receive.should == "LKJSDF"
    # dispatcher.stop
  end

end
