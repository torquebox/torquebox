class CreatePeople < ActiveRecord::Migration
  def self.up
    # ActiveRecord::Base.establish_connection :person_database
    create_table :people do |t|
      t.string :name
      t.integer :age

      t.timestamps
    end
    # ActiveRecord::Base.establish_connection Rails.env
  end

  def self.down
    drop_table :people
  end
end
