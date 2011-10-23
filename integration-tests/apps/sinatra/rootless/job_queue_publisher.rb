require 'torquebox-messaging'

class JobQueuePublisher

  include TorqueBox::Injectors

  def initialize
    @queue = inject( '/queues/jobs' )
  end
  def run
    @queue.publish "employment!"
  end
end
