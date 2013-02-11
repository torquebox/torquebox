require 'bundler/setup'
require 'sinatra'

class SinatraSessions < Sinatra::Base
  # Enable TorqueBox sessions
  if ENV['ALL_SESSION_COOKIE_OPTIONS']
    use TorqueBox::Session::ServletStore, {
      :key => 'sinatra_sessions',
      :domain => 'foobar.com',
      :path => '/baz',
      :httponly => true,
      :secure => true,
      :max_age => 60,
      :timeout => 180
    }
  else 
    use TorqueBox::Session::ServletStore
  end

  get '/foo' do
    session[:message] = 'Hello World!'
    redirect to('bar')
  end

  get '/bar' do
    session[:message]   # => 'Hello World!'
  end

  get '/inactive_interval' do
    env['servlet_request'].session.max_inactive_interval.to_s
  end

  get '/set_value' do
    session[:value] = 'the value'
    @value = session[:value]
    erb :get_value
  end

  get '/get_value' do
    @value = session[:value]
    erb :get_value
  end
end

run SinatraSessions
