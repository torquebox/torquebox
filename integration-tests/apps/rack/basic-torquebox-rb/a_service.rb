class AService

  def initialize(opts)
    @options = opts
  end

  def start
    queue = TorqueBox.fetch('/queue/a-queue')
    queue.publish( @options['foo'] )
  end

  def stop
  end
end
