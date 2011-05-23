class CreateTweetTasks < ActiveRecord::Migration
  def self.up
    create_table :tweet_tasks do |t|
      t.references :user
      t.references :tweet
      t.integer :state
      t.timestamps
    end
  end

  def self.down
    drop_table :tweet_tasks
  end
end
