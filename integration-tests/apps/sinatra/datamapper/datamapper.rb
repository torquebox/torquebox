require 'rubygems'
require 'sinatra'
require 'haml'
require 'dm-core'
require 'datamapper/dm-infinispan-adapter'

get '/' do 
  haml :index
end

get '/muppets' do
  @muppets = Muppet.all
  haml :muppets
end

class Muppet
  include DataMapper::Resource

  property :id,         Serial
  property :num,        Integer
  property :name,       String
  property :bio,        Text, :required => true, :lazy => false
  property :created_at, DateTime

end

DataMapper.setup(:default, :adapter=>'infinispan')
DataMapper::Model.raise_on_save_failure = true 
DataMapper.finalize

Muppet.create(:num=>1, :name=>'Big Bird', :bio=>'Tall, yellow and handsome')
Muppet.create(:num=>2, :name=>'Snuffleupagus', :bio=>"You don't see me")
Muppet.create(:num=>3, :name=>'Cookie Monster', :bio=>"Nom nom nom nom nom")
