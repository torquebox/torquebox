require 'torquebox-messaging'

class SomeTask < TorqueBox::Messaging::Task

  def initialize
    @backchannel = TorqueBox::Messaging::Queue.new( '/queue/backchannel' )
  end
  
  def foo(payload = { })
    @backchannel.publish( 'release' )
    puts 'CALLED'
    'bar'
  end

  def error(payload = { })
    @backchannel.publish( 'release' )
    raise Exception.new('blah')
  end
end
