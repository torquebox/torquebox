module Another
  class SimpleJob

    def initialize(opts)
      @options = opts
      unless java.lang.System.getProperty('org.torquebox.slim_distro')
        @polish     = TorqueBox.fetch( Java::pl.softwaremine.ThingThree )
      end
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
      if java.lang.System.getProperty('org.torquebox.slim_distro') || @polish
        @response_queue.publish( 'done' )
      end
    end

  end
end
