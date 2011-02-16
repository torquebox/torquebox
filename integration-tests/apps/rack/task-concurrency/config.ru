require 'rack_app.rb'
use Rack::Reloader
run RackApp.new 