require 'rubygems'
require 'bundler/setup'
require 'sinatra'

APP='external'
require 'app'

run Sinatra::Application
