require 'rubygems'
require 'bundler/setup'
require 'sinatra'
require 'torquebox-messaging'

publisher = TorqueBox.fetch( '/queues/requests' )
receiver  = TorqueBox.fetch( '/queues/responses' )

get '/up/:word' do
  puts "publishing #{params[:word]}"
  publisher.publish params[:word]
  puts "published #{params[:word]}"
  result = receiver.receive(:timeout => 45000)
  puts "received: #{result}"
  result
end

run Sinatra::Application
