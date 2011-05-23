class CreateTweets < ActiveRecord::Migration
  def self.up
    create_table :tweets do |t|
      t.string :user_id
      t.string :text, :length => 140
      t.datetime :tweet_time
      t.string :location
      t.decimal :lat, :precision => 7, :scale => 4
      t.decimal :long, :precision => 7, :scale => 4
      t.timestamps
    end
  end

  def self.down
    drop_table :tweets
  end
end
