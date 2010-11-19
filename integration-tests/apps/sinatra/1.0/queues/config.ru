require 'rubygems'
require 'bundler/setup'
require 'sinatra'
require 'org.torquebox.torquebox-messaging-client'

publisher = TorqueBox::Messaging::Queue.new '/queues/requests'
receiver = TorqueBox::Messaging::Queue.new '/queues/responses'

get '/:word' do
  publisher.publish params[:word]
  receiver.receive(:timeout => 10000)
end

run Sinatra::Application
