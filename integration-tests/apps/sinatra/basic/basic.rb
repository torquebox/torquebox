
require 'rubygems'
require 'sinatra'

options '/' do
  response.headers['Access-Control-Allow-Origin'] = '*'
  response.headers['Access-Control-Allow-Methods'] = 'POST'
end

get '/' do
  headers 'Biscuit' => 'Gravy'
  erb :index
end

get '/request-mapping' do
  haml :request_mapping
end

get '/poster' do
  haml :poster
end

post '/poster' do
  haml :posted
end
