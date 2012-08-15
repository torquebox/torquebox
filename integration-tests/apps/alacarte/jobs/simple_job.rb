class SimpleJob

  include TorqueBox::Injectors

  def initialize(opts)
    @options = opts
    @polish     = fetch( Java::pl.softwaremine.ThingThree )
    @response_queue = fetch( '/queue/response' )
    @init_params_queue = fetch( '/queue/init_params' )
    @init_params_queue.publish( @options )
    @error_queue = fetch( '/queue/error' )
  end

  def run()
    raise if @options['raise_error']
    $stderr.puts "Job executing! queue is #{@response_queue} and polish is #{@polish}"
    @response_queue.publish( 'done' ) if @polish
  end

  def on_error(error)
    @error_queue.publish( 'an error' )
  end

end
