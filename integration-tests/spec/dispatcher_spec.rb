require 'org.torquebox.torquebox-messaging-client'
require 'org.torquebox.torquebox-messaging-container'

QUEUE_QUESTIONS = TorqueBox::Messaging::Queue.new("/queues/questions")
QUEUE_ANSWERS   = TorqueBox::Messaging::Queue.new("/queues/answers")

class Upcaser < TorqueBox::Messaging::MessageProcessor
  def on_message(body)
    QUEUE_ANSWERS.publish( body.upcase )
  end
end

describe "dispatcher test" do

  deploy :path => 'messaging/queues.yml'

  it "should create dynamic queues and consumers" do
    dispatcher = TorqueBox::Messaging::Dispatcher.new do
      map Upcaser, QUEUE_QUESTIONS
    end
    dispatcher.start
    QUEUE_QUESTIONS.publish "lkjsdf"
    QUEUE_ANSWERS.receive(:timeout => 2000).should == "LKJSDF"
    dispatcher.stop
  end

end
