puts "requiring torquebox-messaging"
require 'torquebox-messaging'
puts "required torquebox-messaging"

class SimpleService

  include TorqueBox::Injectors

  def initialize(opts={})
    @options = opts
    raise 'not Hash' unless @options.is_a?(Hash)
    @something  = fetch( org.torquebox.ThingOne )
    @polish     = fetch( Java::pl.softwaremine.ThingThree )
    @logger     = TorqueBox::Logger.new(self.class)
    @response_queue = fetch( '/queue/response' )
    @next_response_queue = fetch( '/queue/next_response' )
    @init_params_queue = fetch( '/queue/init_params' )
    @queue = @response_queue
  end

  def start()
    @should_run = true
    spawn_thread()
    @init_params_queue.publish( @options )
  end

  def spawn_thread()
    @thread = Thread.new do
      loop_once while @should_run
    end
  end

  def loop_once
    if ( @something && @polish )
      @logger.info "Looping once"
      @logger.info "Sending notice to queue #{@queue}"
      @queue.publish( 'done' )
      @logger.info "Sent and sleep"
    end
    sleep( 1 )
  end

  def stop()
    @should_run = false
    @thread.join
    @queue = @next_response_queue
  end

end
