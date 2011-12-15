#!/usr/bin/env rackup
# encoding: utf-8

# This file can be used to start Padrino,
# just execute it from the command line.

puts "TC: " + __FILE__
puts File.expand_path("../config/boot.rb", __FILE__)
puts RACK_ROOT
puts $:
require File.expand_path("../config/boot.rb", __FILE__)

run Padrino.application
