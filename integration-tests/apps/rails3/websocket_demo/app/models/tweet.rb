class Tweet < ActiveRecord::Base
  
  validates_presence_of :user_id, :text, :tweet_time
  
  def as_json(options = {})
    super(:except => [:created_at, :updated_at])
  end
  
end
