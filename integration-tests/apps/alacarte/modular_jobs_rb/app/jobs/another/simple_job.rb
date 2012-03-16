module Another
  class SimpleJob
  
    include TorqueBox::Injectors
  
    def initialize(opts)
      @options = opts
      @polish     = inject( Java::pl.softwaremine.ThingThree )
      @response_queue = inject( '/queue/response' )
      @init_params_queue = inject( '/queue/init_params' )
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
