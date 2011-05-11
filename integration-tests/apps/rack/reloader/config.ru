require 'rack_app.rb'

use Rack::Reloader, 0.000001

run RackApp.new 
