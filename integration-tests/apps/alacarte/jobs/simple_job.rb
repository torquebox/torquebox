class SimpleJob

  include TorqueBox::Injectors

  def initialize(opts)
    @options = opts
    @polish     = inject( Java::pl.softwaremine.ThingThree )
    @response_queue = inject( '/queue/response' )
    @init_params_queue = inject( '/queue/init_params' )
    @init_params_queue.publish( @options )
  end

  def run()
    $stderr.puts "Job executing! queue is #{@response_queue} and polish is #{@polish}"
    @response_queue.publish( 'done' ) if @polish
  end

end
