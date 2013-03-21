class SimpleJob

  def initialize(opts)
    @options = opts
    @response_queue = TorqueBox.fetch( '/queue/response' )
    @init_params_queue = TorqueBox.fetch( '/queue/init_params' )
    @init_params_queue.publish( @options )
  end

  def run()
    # Ensure we can access Rails models
    Tweet.count
    @response_queue.publish( 'done' )
  end

end
