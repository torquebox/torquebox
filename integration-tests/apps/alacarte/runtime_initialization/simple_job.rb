class SimpleJob

  include TorqueBox::Injectors

  def initialize()
    @queue = inject( '/queues/jobs_context' )
  end

  def run()
    @queue.publish( ENV['TORQUEBOX_CONTEXT'] )
  end

end

