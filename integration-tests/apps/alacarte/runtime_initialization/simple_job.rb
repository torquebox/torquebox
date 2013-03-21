class SimpleJob

  def initialize()
    @queue = TorqueBox.fetch( '/queues/jobs_context' )
  end

  def run()
    @queue.publish( ENV['TORQUEBOX_CONTEXT'] )
  end

end

