class Term
  include DataMapper::Resource

  property :id, Serial
  property :term, String, :required => true, :length => 50

  validates_uniqueness_of :term
end

