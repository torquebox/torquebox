require 'torquebox-core'

class RackApp
  include TorqueBox::Injectors
  
  def call(env)
    msg = env['QUERY_STRING']
    destination = if msg.start_with?('topic')
                    fetch('/topics/test')
                  elsif msg.start_with?('parentless')
                    fetch('/queues/parentless')
                  else
                    fetch('/queues/test')
                  end

    destination.publish(msg, :priority => rand(10))
    
    [200, { 'Content-Type' => 'text/html' }, "<div id='success'>it worked</div>"] 
  end
end

run RackApp.new
