class User < ActiveRecord::Base
  
  acts_as_authentic do |c|
  end
  
  def as_json(options = {})
    super(:only => :username)
  end
  
end
