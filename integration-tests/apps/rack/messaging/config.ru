require 'torquebox-core'
require 'torquebox-messaging'

class RackApp
  include TorqueBox::Injectors
  
  def call(env)
    case env['PATH_INFO']
    when "/start"
      q = TorqueBox::Messaging::Queue.start( env['QUERY_STRING'] )
      puts "started #{q.name}"
    when '/stop'
      q = TorqueBox::Messaging::Queue.new( env['QUERY_STRING'] )
      q.stop
      puts "stopped #{q.name}"
    else
      msg = env['QUERY_STRING']
      queue = inject('/queues/test')
      topic = inject('/topics/test')
      msg.include?('topic') ? topic.publish(msg) : queue.publish(msg)
    end
    [200, { 'Content-Type' => 'text/html' }, "<div id='success'>it worked</div>"] 
  end
end

run RackApp.new
