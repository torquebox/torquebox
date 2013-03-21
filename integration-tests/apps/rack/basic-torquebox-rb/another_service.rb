class AnotherService

  def initialize(opts={})
    @options = opts
  end

  def start
    queue = TorqueBox.fetch('/queue/another-queue')
    message = @options['flavor'] ? @options['flavor'] : 'no message'
    queue.publish( message )
  end

  def stop
  end
end
