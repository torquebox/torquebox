require 'data_mapper'

class Url
  include DataMapper::Resource

  property :id, Serial
  property :url, String, :required => true, :length => 20
  property :title, String, :length => 200
  property :count, Integer

  validates_uniqueness_of :url
end
