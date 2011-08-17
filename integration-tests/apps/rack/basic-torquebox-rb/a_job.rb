class AJob
  include TorqueBox::Injectors
  
  def initialize(opts = { })
    @options = opts
  end
  
  def run
    queue = inject('/queue/job-queue')
    queue.publish( @options['ham'] ) if @options['ham']
  end
end
