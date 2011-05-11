require 'torquebox-messaging'

class SimpleService 
  include TorqueBox::Injectors

  def initialize(opts={})
    puts "init"
    @webserver  = inject_mc('jboss.web:service=WebServer')
    @something  = inject( org.torquebox.ThingOne )
    @polish     = inject( Java::pl.softwaremine.ThingThree )
    @logger     = TorqueBox::Logger.new(self.class)
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
    if ( @webserver && @something && @polish )
      @logger.info "Looping once"
      TorqueBox::Messaging::Queue.new( '/queue/response' ).publish( 'done' )
    end
  end

  def stop()
    @should_run = false
    @thread.join
  end

end
