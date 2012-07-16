require 'torquebox-messaging'

class MyJob
  def initialize(opts)
    @number = opts['number']
    @queue = TorqueBox::Messaging::Queue.new( '/queue/backchannel' )
  end

  def run()
    @queue.publish("job-#{@number}")
    sleep(10)
  end

end

