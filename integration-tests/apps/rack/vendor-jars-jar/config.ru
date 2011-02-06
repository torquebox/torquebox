
# rackup file for RackApp 
require 'rack_app.rb' 

#use Rack::Reloader 
use Rack::ShowExceptions # line 5 
use Rack::CommonLogger 

run RackApp.new 
