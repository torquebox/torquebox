require 'bundler/setup'
require 'sinatra'

class SinatraSessions < Sinatra::Base
  # Enable TorqueBox sessions
  use TorqueBox::Session::ServletStore

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
end

run SinatraSessions
