require 'torquebox-messaging'

class JobQueuePublisher

  include TorqueBox::Injectors

  def initialize
    @queue = inject( 'queue/jobs' )
  end
  def run
    @queue.publish "employment!"
  end
end
