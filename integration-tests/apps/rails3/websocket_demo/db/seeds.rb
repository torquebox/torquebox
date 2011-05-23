# This file should contain all the record creation needed to seed the database with its default values.
# The data can then be loaded with the rake db:seed (or created alongside the db with db:setup).
#
# Examples:
#
#   cities = City.create([{ :name => 'Chicago' }, { :name => 'Copenhagen' }])
#   Mayor.create(:name => 'Daley', :city => cities.first)
User.create(:username => 'alice', :password => 'alice', :password_confirmation => 'alice')
User.create(:username => 'bob', :password => 'bobomb', :password_confirmation => 'bobomb')
User.create(:username => 'eve', :password => 'evesix', :password_confirmation => 'evesix')
User.create(:username => 'mallory', :password => 'mallory', :password_confirmation => 'mallory')