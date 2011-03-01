#!/usr/bin/env jruby

require 'rubygems'
require 'torquebox-messaging'

TorqueBox::Messaging::Queue.new(ARGV[0]).publish ARGV[1]
