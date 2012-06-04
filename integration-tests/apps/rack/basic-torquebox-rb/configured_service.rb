class ConfiguredService
  include TorqueBox::Injectors

  def initialize(opts={})
    @options = opts
  end
  
  def start
    queue = fetch('/queue/flavor-queue')
    message = @options['flavor'] ? @options['flavor'] : 'no message'
    queue.publish( message )
  end

  def stop
  end
end
