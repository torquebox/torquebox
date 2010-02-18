#!/usr/bin/env jruby

require 'rubygems'
require 'torquebox-messaging-tasks'

TorqueBox::Messaging::Tasks.enqueue( 'emailer#send_password_reset', { :time=>Time.now, :user=>22 } )

