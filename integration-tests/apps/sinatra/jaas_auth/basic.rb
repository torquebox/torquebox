
require 'rubygems'
require 'sinatra'
require 'torquebox-security'

settings.static = false

get '/' do
  authenticator = TorqueBox::Authentication[:pork]
  @authenticated = authenticator.authenticate('crunchy', 'bacon')
  haml '#auth= @authenticated'
end
