class TweetTask < ActiveRecord::Base
  
  ASSIGNED = 1
  COMPLETED = 2

  belongs_to :tweet
  belongs_to :user
  
end
