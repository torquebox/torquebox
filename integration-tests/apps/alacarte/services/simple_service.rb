require 'torquebox-messaging'

class SimpleService 

  include TorqueBox::Injectors

  def initialize(opts={})
    puts "init"
    @something  = inject( org.torquebox.ThingOne )
    @polish     = inject( Java::pl.softwaremine.ThingThree )
    @logger     = TorqueBox::Logger.new(self.class)

   puts "something=#{@sometime}"
   puts "polish=#{@polish}"
   puts "logger=#{@logger}"
  end

  def start() 
    puts "start"
    @should_run = true
    spawn_thread()
  end

  def spawn_thread()
    puts "spawn_thread"
    @thread = Thread.new do
      loop_once while @should_run 
    end
  end

  def loop_once
    puts "loop_once"
    if ( @something && @polish )
      @logger.info "Looping once"
      response_queue = inject( 'queue/response' )
      @logger.info "Sending notice to queue #{response_queue}"
      response_queue.publish( 'done' )
      @logger.info "Sent and sleep"
    end
    sleep( 1 ) 
  end

  def stop()
    @should_run = false
    @thread.join
  end

end
