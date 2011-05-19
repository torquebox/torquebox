require 'torquebox-core'
require 'torquebox-messaging'

class RackApp
  include TorqueBox::Injectors
  
  def call(env)
    puts "Invoking app"
    puts env.inspect
    msg = env['QUERY_STRING']
    queue = inject('/queues/test')
    queue.publish(msg)
    [200, { 'Content-Type' => 'text/html' }, "<div id='success'>it worked</div>"] 
  end
end

run RackApp.new
