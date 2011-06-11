require 'torquebox-messaging'

class Something
  include TorqueBox::Messaging::Backgroundable
  
  always_background :foo, :error

  def initialize
    @backchannel = TorqueBox::Messaging::Queue.new( '/queue/backchannel' )
  end
  def foo
    @backchannel.publish( 'release' )
    puts 'CALLED'
    'bar'
  end

  def error
    @backchannel.publish( 'release' )
    raise Exception.new('blah')
  end
end
