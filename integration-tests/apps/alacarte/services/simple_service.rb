require 'torquebox-messaging'

class SimpleService

  include TorqueBox::Injectors

  def initialize(opts={})
    @options = opts
    @something  = inject( org.torquebox.ThingOne )
    @polish     = inject( Java::pl.softwaremine.ThingThree )
    @logger     = TorqueBox::Logger.new(self.class)
    @response_queue = inject( '/queue/response' )
    @next_response_queue = inject( '/queue/next_response' )
    @init_params_queue = inject( '/queue/init_params' )
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
