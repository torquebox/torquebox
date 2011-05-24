
require 'torquebox-security'

app = lambda { |env| 

  puts "Invoking app"
  authenticator = TorqueBox::Authentication[ 'test-jaas' ]
  puts "Got authenticator: " + authenticator.inspect

  message = "this shouldn't happen"

  req = Rack::Request.new(env)
  if req.path_info == "/success"
    message = "it worked" if authenticator.authenticate('guest', nil)
  else
    message = "it failed" unless authenticator.authenticate('foo', 'bar') 
  end
  [200, { 'Content-Type' => 'text/html' }, "<div id='success' class='basic-rack #{ENV['GRIST']}'>#{message}</div>"] 
}
run app

