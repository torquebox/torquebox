
require 'rubygems'
require 'sinatra'

get '/' do
  erb :index
end

get '/request-mapping' do
  haml :request_mapping
end
