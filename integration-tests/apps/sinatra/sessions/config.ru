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
end

run SinatraSessions
