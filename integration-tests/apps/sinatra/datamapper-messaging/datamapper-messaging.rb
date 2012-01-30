require 'rubygems'
require 'sinatra'
require 'database'

get '/foo/:message' do
  Foo.create.foo(params[:message])
  "success"
end

get '/bar/:message' do
  Bar.create.background.bar(params[:message])
  "success"
end



