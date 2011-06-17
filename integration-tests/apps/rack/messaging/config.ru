require 'torquebox-core'

class RackApp
  include TorqueBox::Injectors
  
  def call(env)
    queue = inject('/queues/test')
    topic = inject('/topics/test')
    msg = env['QUERY_STRING']
    msg.include?('topic') ? topic.publish(msg) : queue.publish(msg)
    [200, { 'Content-Type' => 'text/html' }, "<div id='success'>it worked</div>"] 
  end
end

run RackApp.new
