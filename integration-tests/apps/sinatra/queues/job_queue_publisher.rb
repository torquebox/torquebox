require 'torquebox-messaging'

class JobQueuePublisher

  include TorqueBox::Injectors

  def initialize
    #@queue = TorqueBox::Messaging::Queue.new '/queues/jobs'
    @queue = inject( '/queues/jobs' )
  end
  def run
    @queue.publish "employment!"
  end
end
