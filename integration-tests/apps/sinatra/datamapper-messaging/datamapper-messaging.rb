require 'rubygems'
require 'sinatra'
require 'data_mapper'
require 'dm-sqlite-adapter'

DataMapper::Logger.new($stdout, :debug)
DataMapper::Model.raise_on_save_failure = true 
DataMapper.setup(:default, 'sqlite:///tmp/dm-messaging-test.db')

require 'foo'
require 'bar'

DataMapper.auto_upgrade!
DataMapper.finalize

get '/foo/:message' do
  Foo.create.foo(params[:message])
  "success"
end

get '/bar/:message' do
  Bar.create.background.bar(params[:message])
  "success"
end



