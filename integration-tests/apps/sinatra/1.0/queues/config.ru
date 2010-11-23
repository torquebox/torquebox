require 'rubygems'
require 'bundler/setup'
require 'sinatra'
require 'org.torquebox.torquebox-messaging-client'

publisher = TorqueBox::Messaging::Queue.new '/queues/requests'
receiver = TorqueBox::Messaging::Queue.new '/queues/responses'

get '/:word' do
  puts "JC: publishing #{params[:word]}"
  publisher.publish params[:word]
  puts "JC: published #{params[:word]}"
  puts "JC: receiving..."
  result = receiver.receive(:timeout => 25000)
  puts "JC: received #{result}"
  result
end

run Sinatra::Application
