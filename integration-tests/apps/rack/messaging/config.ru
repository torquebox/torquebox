require 'torquebox-messaging'

app = lambda { |env| 
  puts "Invoking app"
  puts env.inspect
  msg = env['QUERY_STRING']
  TorqueBox::Messaging::Queue.new('/queues/test').publish(msg)
  [200, { 'Content-Type' => 'text/html' }, "<div id='success'>it worked</div>"] 
}
run app
