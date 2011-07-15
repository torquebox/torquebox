require 'sinatra'
require 'something'
require 'app/tasks/some_task'

before do
  @backchannel = TorqueBox::Messaging::Queue.new( '/queue/backchannel' )
  if params[:task]
    @something = Object.new
    def @something.method_missing(meth)
      SomeTask.async(meth)
    end
  else
    @something = Something.new
  end
end


get '/should_work' do
  future = @something.foo
  @backchannel.receive( :timeout => 120_000 )
  @result = future.result( 10_000 )
  @status = future_status( future )

  haml :result
end

get '/should_raise_error' do
  future = @something.error
  @backchannel.receive( :timeout => 120_000 )
  begin
    future.result( 10_000 )
  rescue Exception => ex
    @status = future_status( future )
  end

  haml :result
end

get '/should_set_status' do
  future = @something.with_status
  wait_for { future.started? }
  wait_for { future.status_changed? }
  @interim_status = future.status
  @backchannel.publish( 'ack' )
  wait_for { future.complete? }
  @result = future.result
  @final_status = future.status

  haml :status_result
end

def wait_for
  wait_time = 0
  until yield || wait_time > 10
    sleep(0.1)
    wait_time += 0.1
  end
end

def future_status(future)
  status = []
  %w{ started complete error }.each do |meth|
    status << meth if future.send("#{meth}?")
  end
  status
end
