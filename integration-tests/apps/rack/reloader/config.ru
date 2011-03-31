require 'rack_app.rb'

use Rack::Reloader, 2

run RackApp.new 
