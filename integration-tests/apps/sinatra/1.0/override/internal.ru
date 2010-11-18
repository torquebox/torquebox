require 'rubygems'
require 'bundler/setup'

APP='internal'
require 'app'

run Sinatra::Application
