class AJob
  include TorqueBox::Injectors
  
  def initialize(opts = { })
    @options = opts
  end
  
  def run
    queue = fetch('/queue/job-queue')
    message = @options['ham'] ? @options['ham'] : 'no message'
    queue.publish message
  end
end
