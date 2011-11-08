require 'spec_helper'

remote_describe "sequel support" do
  it "should work for in-memory H2 databases" do
    require 'rubygems'
    require 'sequel'
    lambda {
      db = Sequel.connect("jdbc:h2:mem:ham;DB_CLOSE_DELAY=-1")
      db.create_table :biscuits do
        primary_key :id
      end
      db[:biscuits].count
    }.should_not raise_error
  end
end
