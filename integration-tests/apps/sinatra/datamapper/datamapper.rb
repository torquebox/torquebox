require 'rubygems'
require 'sinatra'
require 'haml'
require 'dm-core'

get '/' do 
  haml :index
end

class Muppet
  include DataMapper::Resource

  property :id,         Serial
  property :name,       String
  property :bio,        Text, :required => true, :lazy => false
  property :created_at, DateTime

end

DataMapper.setup(:default, :adapter=>'infinispan')
DataMapper::Model.raise_on_save_failure = true 
DataMapper.finalize
DataMapper.auto_upgrade!
