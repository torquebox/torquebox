require 'torquebox-messaging'

class JobQueuePublisher

  include TorqueBox::Injectors

  def initialize
    @queue = fetch( '/queues/jobs' )
  end
  def run
    @queue.publish "employment!"
  end
end
