#!/usr/bin/env jruby

require 'rubygems'
require 'org.torquebox.torquebox-messaging-client'

TorqueBox::Messaging::Queue.new(ARGV[0]).publish ARGV[1]
