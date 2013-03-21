module Another
  class SimpleJob

    def initialize(opts)
      @options = opts
      @polish     = TorqueBox.fetch( Java::pl.softwaremine.ThingThree )
      @response_queue = TorqueBox.fetch( '/queue/response' )
      @init_params_queue = TorqueBox.fetch( '/queue/init_params' )
      puts "publishing #{@options.inspect}"
      puts "dump pre"
      Marshal.dump( @options )
      puts "dump post"
      puts "keys #{@options.keys.inspect}" 
      puts "array #{@options['an_array'].inspect}"
      puts "color #{@options['color'].inspect}"
      @init_params_queue.publish( @options )
    end

    def run()
      $stderr.puts "Another::SimpleJob executing! queue is #{@response_queue} and polish is #{@polish}"
      @response_queue.publish( 'done' ) if @polish
    end

  end
end
