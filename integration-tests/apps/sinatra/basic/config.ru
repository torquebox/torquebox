require 'rubygems'
require 'bundler/setup'

require 'basic'

use Rack::CommonLogger, TorqueBox::Logger.new

run Sinatra::Application
