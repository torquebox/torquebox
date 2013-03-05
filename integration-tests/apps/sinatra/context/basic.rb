require 'rubygems'
require 'sinatra'

get '/' do
  headers 'Biscuit' => 'Gravy'
  "Index"
end

get '/something' do
  "Something"
end
