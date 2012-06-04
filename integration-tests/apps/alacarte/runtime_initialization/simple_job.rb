class SimpleJob

  include TorqueBox::Injectors

  def initialize()
    @queue = fetch( '/queues/jobs_context' )
  end

  def run()
    @queue.publish( ENV['TORQUEBOX_CONTEXT'] )
  end

end

