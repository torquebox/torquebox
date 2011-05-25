
require 'torquebox-security'

app = lambda { |env| 

  puts "Invoking app"

  message = "this shouldn't happen"

  req = Rack::Request.new(env)
  if req.path_info == "/torquebox-global-success"
    authenticator = TorqueBox::Authentication[ 'global' ]
    message = "it worked" if authenticator.authenticate('scott', 'scott')
  elsif req.path_info == "/torquebox-global-guest"
    authenticator = TorqueBox::Authentication[ 'global' ]
    message = "it worked" if authenticator.authenticate('guest', nil)
  elsif req.path_info == "/torquebox-global-failure"
    authenticator = TorqueBox::Authentication[ 'global' ]
    message = "it worked" unless authenticator.authenticate('foo', 'bar') 
  elsif req.path_info == "/torquebox-local-success"
    authenticator = TorqueBox::Authentication[ 'local' ]
    message = "it worked" if authenticator.authenticate('foo', 'bar') 
  elsif req.path_info == "/torquebox-local-failure"
    authenticator = TorqueBox::Authentication[ 'local' ]
    message = "it worked" unless authenticator.authenticate('boo', 'far') 
  end
  [200, { 'Content-Type' => 'text/html' }, "<div id='success' class='basic-rack #{ENV['GRIST']}'>#{message}</div>"] 
}
run app

