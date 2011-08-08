
require 'rubygems'
require 'sinatra'

require 'org/torquebox/auth/authentication'


# Obviously you wouldn't authenticate this way in real life
get '/default/:username/:password' do
  @login_status = "failure"
  authenticator = TorqueBox::Authentication.default
  authenticator.authenticate(params[:username], params[:password]) do
    @login_status = "success"
  end
  haml :auth
end

# The configured HsqlDbRealm domain uses a blank password:w
get '/configured/:username/:password' do
  @login_status = "failure"
  authenticator = TorqueBox::Authentication['configured']
  authenticator.authenticate(params[:username], params[:password]) do
    @login_status = "success"
  end
  haml :auth
end



