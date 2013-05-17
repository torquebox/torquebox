class SimpleJob

  include TorqueBox::Injectors

  def initialize(opts)
    @options = opts
    unless java.lang.System.getProperty('org.torquebox.slim_distro')
      @polish     = fetch( Java::pl.softwaremine.ThingThree )
    end
    @response_queue = fetch( '/queue/response' )
    @init_params_queue = fetch( '/queue/init_params' )
    @init_params_queue.publish( @options )
    @error_queue = fetch( '/queue/error' )
  end

  def run()
    raise if @options['raise_error']
    if java.lang.System.getProperty('org.torquebox.slim_distro') || @polish
      @response_queue.publish( 'done' )
    end
  end

  def on_error(error)
    @error_queue.publish( 'an error' )
  end

end
