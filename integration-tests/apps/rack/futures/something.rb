require 'torquebox-messaging'

class Something
  include TorqueBox::Messaging::Backgroundable
  
  always_background :foo, :error, :with_status

  def initialize
    @backchannel = TorqueBox::Messaging::Queue.new( '/queue/backchannel' )
  end
  
  def foo
    @backchannel.publish( 'release' )
    puts 'CALLED'
    'bar'
  end

  def with_status
    future.status = '1'
    future.status = '2'
    @backchannel.receive( :timeout => 1_000 )
  end
    

  def error
    @backchannel.publish( 'release' )
    raise Exception.new('blah')
  end
end
