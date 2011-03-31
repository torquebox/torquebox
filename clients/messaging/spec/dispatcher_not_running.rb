#require 'spec_helper'

require 'rubygems'
require 'torquebox-naming'
require 'torquebox-naming-container'
require 'torquebox-messaging-container'
require 'torquebox/messaging/message_processor'
require 'torquebox/messaging/destination'

QUEUE_QUESTIONS = TorqueBox::Messaging::Queue.new("/queues/questions")
QUEUE_ANSWERS   = TorqueBox::Messaging::Queue.new("/queues/answers")

TESTING_ON_WINDOWS = ( java.lang::System.getProperty( "os.name" ) =~ /.*windows*/i )

class Upcaser < TorqueBox::Messaging::MessageProcessor
  def on_message(body)
    QUEUE_ANSWERS.publish( body.upcase )
  end
end

describe "dispatcher test" do
  before(:each) do
    @container = TorqueBox::Container::Foundation.new
    @container.enable( TorqueBox::Naming::NamingService ) {|config| config.export=true}
    @container.enable( TorqueBox::Messaging::MessageBroker ) 
    @container.start

    @queues_yml = @container.deploy( File.join( File.dirname(__FILE__), 'dispatcher-queues.yml' ) )
    @container.process_deployments(true)
    puts "Deployed queues"
  end
  
  after(:each) do
    puts "UNDEPLOY queues"
    @container.undeploy( @queues_yml )
    puts "UNDEPLOYED queues"
    puts "STOP container"
    @container.stop
    puts "STOPPED container"
  end

  unless TESTING_ON_WINDOWS
    it "should associate a processor with a queue" do
      dispatcher = TorqueBox::Messaging::Dispatcher.new( :skip_naming=>true ) do
        map Upcaser, QUEUE_QUESTIONS
      end
      dispatcher.start
      QUEUE_QUESTIONS.publish "lkjsdf"
      QUEUE_ANSWERS.receive(:timeout => 2000).should == "LKJSDF"
      dispatcher.stop
    end
  end

end
