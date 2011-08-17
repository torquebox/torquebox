class SimpleJob

  include TorqueBox::Injectors

  def initialize(opts)
    @options = opts
    @response_queue = inject( '/queue/response' )
    @init_params_queue = inject( '/queue/init_params' )
    @init_params_queue.publish( @options )
  end

  def run()
    $stderr.puts "Job executing! queue is #{@response_queue}"
    @response_queue.publish( 'done' )
  end

end
