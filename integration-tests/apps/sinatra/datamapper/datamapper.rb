require 'rubygems'
require 'sinatra'
require 'haml'
require 'dm-core'
require 'torquebox-infinispan'

get '/' do 
  haml :index
end

get '/muppets' do
  @muppets = Muppet.all
  haml :muppets
end

get '/muppet/name' do
  @snuffy = Muppet.first(:name=>'Snuffleupagus')
  haml :muppet
end

get '/muppet/id' do
  @snuffy = Muppet.get(2)
  haml :muppet
end

get '/muppet/num' do
  @snuffy = Muppet.first(:num=>20)
  haml :muppet
end

get '/muppet/range' do
  @snuffy = Muppet.first(:num.gt => 10, :num.lt => 30)
  haml :muppet
end

get '/muppet/inclusive-range' do
  @snuffy = Muppet.first(:num.gte => 20, :num.lte => 30)
  haml :muppet
end

get '/muppet/like' do
  @snuffy = Muppet.first(:bio.like => '%see me')
  haml :muppet
end

get '/muppet/delete' do
  Muppet.find(:name=>'Snuffleupagus').destroy
  "Hiding" unless Muppet.find(:name=>'Snuffleupagus')
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

Muppet.create(:num=>10, :name=>'Big Bird', :bio=>'Tall, yellow and handsome')
Muppet.create(:num=>20, :name=>'Snuffleupagus', :bio=>"You don't see me")
Muppet.create(:num=>30, :name=>'Cookie Monster', :bio=>"Nom nom nom nom nom")
