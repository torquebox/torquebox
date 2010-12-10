require 'rubygems'
require 'bundler/setup'

ENV['APP'] = 'internal'
require 'app'

run Sinatra::Application
