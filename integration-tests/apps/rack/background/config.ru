require 'rack_app'
use Rack::Reloader
run RackApp.new 
