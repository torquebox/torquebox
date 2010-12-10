require 'rubygems'
require 'bundler/setup'

ENV['APP'] = 'external'
require 'app'

run Sinatra::Application
