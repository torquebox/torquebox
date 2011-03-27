require 'rubygems'
require 'bundler/setup'
require 'sinatra'
require 'torquebox-messaging'

publisher = TorqueBox::Messaging::Queue.new '/queues/requests'
receiver = TorqueBox::Messaging::Queue.new '/queues/responses'

#extend TorqueBox::Injectors
#publisher = inject( '/queues/requests' )
#receiver  = inject( '/queues/responses' )

get '/up/:word' do
  puts "publishing #{params[:word]}"
  publisher.publish params[:word]
  puts "published #{params[:word]}"
  result = receiver.receive(:timeout => 25000)
  puts "received: #{result}"
  result
end

get '/job' do
  TorqueBox::Messaging::Queue.new('/queues/jobs').receive(:timeout => 25000)
  #inject('/queues/jobs').receive(:timeout => 25000)
end

run Sinatra::Application
