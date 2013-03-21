require 'torquebox-core'

class RackApp
  
  def call(env)
    msg = env['QUERY_STRING']
    destination = if msg.start_with?('topic')
                    TorqueBox.fetch('/topics/test')
                  elsif msg.start_with?('parentless')
                    TorqueBox.fetch('/queues/parentless')
                  else
                    TorqueBox.fetch('/queues/test')
                  end

    destination.publish(msg, :priority => rand(10))
    
    [200, { 'Content-Type' => 'text/html' }, "<div id='success'>it worked</div>"] 
  end
end

run RackApp.new
