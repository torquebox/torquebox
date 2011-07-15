require 'torquebox-messaging'

class SomeTask < TorqueBox::Messaging::Task

  def initialize
    @backchannel = TorqueBox::Messaging::Queue.new( '/queue/backchannel' )
    @ack = TorqueBox::Messaging::Queue.new( '/queue/ack' )
  end
  
  def foo(payload = { })
    @backchannel.publish( 'release' )
    puts 'CALLED'
    'bar'
  end
  
  def with_status(payload={ })
    future.status = '1'
    future.status = '2'
    future.status = '3'
    future.status = '4'
    @ack.receive( :timeout => 10_000 )
    'ding'
  end
    

  def error(payload = { })
    @backchannel.publish( 'release' )
    raise Exception.new('blah')
  end
end
