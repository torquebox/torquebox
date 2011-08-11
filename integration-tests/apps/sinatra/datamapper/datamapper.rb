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

get '/muppet/date/range' do
  start  = DateTime.parse((Date.today-1).to_s)
  ending = DateTime.parse((Date.today+1).to_s)
  result = Muppet.all(:created_at => (start..ending), :order=>:created_at.asc)
  @snuffy = result[1]
  haml :muppet
end

get '/muppet/delete' do
  Muppet.destroy
  "Hiding" unless Muppet.count > 0
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

Muppet.create(:num=>10, :name=>'Big Bird', :bio=>'Tall, yellow and handsome', :created_at => DateTime.parse(Date.today.to_s))
Muppet.create(:num=>20, :name=>'Snuffleupagus', :bio=>"You don't see me", :created_at => DateTime.parse((Date.today +1).to_s))
Muppet.create(:num=>30, :name=>'Cookie Monster', :bio=>"Nom nom nom nom nom", :created_at => DateTime.parse((Date.today -1).to_s))
