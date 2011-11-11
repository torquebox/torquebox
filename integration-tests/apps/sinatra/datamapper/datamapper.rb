require 'rubygems'
require 'sinatra'
require 'haml'
require 'dm-core'
require 'datamapper/dm-infinispan-adapter'

ADAPTER = DataMapper.setup(:default, :adapter=>'infinispan', :persist=>true)

get '/' do 
  @indexed = !ADAPTER.search_manager.nil?
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
  by_name = Muppet.first(:name=>'Snuffleupagus') # assumes that finding by name works...
  @snuffy = Muppet.get(by_name.id)
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

  has 1, :coat
end

class Coat
  include DataMapper::Resource
  property :id,         Serial
  property :color,      String
end

DataMapper::Model.raise_on_save_failure = true 
DataMapper.finalize

today = Date.today
tomorrow = Date.today + 1
yesterday = Date.today - 1

blue   = Coat.create(:color=>'Blue')
brown  = Coat.create(:color=>'Brown')

Muppet.create(:num=>10, :name=>'Big Bird', :bio=>'Tall, yellow and handsome', :created_at => DateTime.parse(yesterday.to_s), :coat => Coat.create(:color=>'Yellow'))
Muppet.create(:num=>20, :name=>'Snuffleupagus', :bio=>"You don't see me", :created_at => DateTime.parse(today.to_s), :coat => brown)
Muppet.create(:num=>30, :name=>'Cookie Monster', :bio=>"Nom nom nom nom nom", :created_at => DateTime.parse(tomorrow.to_s), :coat => blue)


