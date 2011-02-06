require 'rubygems'
require 'bundler/setup'
require 'sinatra'
require 'org.torquebox.torquebox-messaging-client'

publisher = TorqueBox::Messaging::Queue.new '/queues/requests'
receiver = TorqueBox::Messaging::Queue.new '/queues/responses'

get '/up/:word' do
  publisher.publish params[:word]
  receiver.receive(:timeout => 25000)
end

get '/job' do
  TorqueBox::Messaging::Queue.new('/queues/jobs').receive(:timeout => 25000)
end

run Sinatra::Application
