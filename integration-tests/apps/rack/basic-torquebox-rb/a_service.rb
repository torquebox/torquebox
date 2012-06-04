class AService
  include TorqueBox::Injectors
  
  def initialize(opts)
    @options = opts
  end
  
  def start
    queue = fetch('/queue/a-queue')
    queue.publish( @options['foo'] )
  end

  def stop
  end
end
