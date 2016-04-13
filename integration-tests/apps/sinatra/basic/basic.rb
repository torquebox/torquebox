
require 'rubygems'
require 'sinatra'

disable :static, :logging

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

get '/long_body' do
  body = 'foobarbaz' * 50000
  body << "<div id='long_body'>complete</div>"
end

get '/304_response' do
  [304, {}, []]
end