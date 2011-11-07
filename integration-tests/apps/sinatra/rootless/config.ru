require 'rubygems'
require 'bundler/setup'
require 'sinatra'
require 'torquebox-messaging'

extend TorqueBox::Injectors
publisher = inject( '/queues/requests' )
receiver  = inject( '/queues/responses' )

get '/up/:word' do
  puts "publishing #{params[:word]}"
  publisher.publish params[:word]
  puts "published #{params[:word]}"
  result = receiver.receive(:timeout => 45000)
  puts "received: #{result}"
  result
end

run Sinatra::Application
