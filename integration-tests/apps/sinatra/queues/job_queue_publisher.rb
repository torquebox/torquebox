require 'torquebox-messaging'

class JobQueuePublisher

  def initialize
    @queue = TorqueBox.fetch( '/queues/jobs' )
  end
  def run
    @queue.publish "employment!"
  end
end
