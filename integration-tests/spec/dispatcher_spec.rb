require 'spec_helper'

require 'torquebox-messaging'
require 'torquebox-messaging-container'

QUEUE_QUESTIONS = TorqueBox::Messaging::Queue.new("/queues/questions")
QUEUE_ANSWERS   = TorqueBox::Messaging::Queue.new("/queues/answers")

TESTING_ON_WINDOWS = ( java.lang::System.getProperty( "os.name" ) =~ /.*windows*/i )

class Upcaser < TorqueBox::Messaging::MessageProcessor
  def on_message(body)
    QUEUE_ANSWERS.publish( body.upcase )
  end
end

describe "dispatcher test" do

  deploy :path => 'messaging/queues.yml', :ruby_1_9 => false 

  unless TESTING_ON_WINDOWS
    it "should associate a processor with a queue" do
      dispatcher = TorqueBox::Messaging::Dispatcher.new do
        map Upcaser, QUEUE_QUESTIONS
      end
      dispatcher.start
      QUEUE_QUESTIONS.publish "lkjsdf"
      QUEUE_ANSWERS.receive(:timeout => 2000).should == "LKJSDF"
      dispatcher.stop
    end
  end

end
