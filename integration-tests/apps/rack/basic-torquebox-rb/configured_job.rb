class ConfiguredJob

  def initialize(opts = { })
    @options = opts
  end

  def run
    queue = TorqueBox.fetch('/queue/configured-job-queue')
    message = @options['ham'] ? @options['ham'] : 'no message'
    queue.publish message
  end
end
