class LongRunningJob

  include TorqueBox::Injectors

 def initialize()
    @response_queue = inject( '/queue/response' )
  end

  def run()
    $stderr.puts "Job executing! queue is #{@response_queue}"
    @response_queue.publish( 'started' )
    sleep( 5 )
    @response_queue.publish( 'done' )
  end

end
