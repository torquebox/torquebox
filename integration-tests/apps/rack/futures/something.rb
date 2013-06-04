require 'torquebox-messaging'

class Something
  include TorqueBox::Messaging::Backgroundable

  always_background :foo, :error, :with_status, :class_with_status

  def foo
    Something.backchannel.publish( 'release' )
    puts 'CALLED'
    'bar'
  end

  def with_status
    future.status = '1'
    future.status = '2'
    future.status = '3'
    future.status = '4'
    Something.backchannel.receive( :timeout => 10_000 )
    'ding'
  end

  def error
    Something.backchannel.publish( 'release' )
    raise Exception.new('blah')
  end

  def self.backchannel
    TorqueBox::Messaging::Queue.new( '/queue/backchannel' )
  end

  def self.class_with_status
    future.status = '1'
    future.status = '2'
    future.status = '3'
    future.status = '4'
    backchannel.receive( :timeout => 10_000 )
    'ding'
  end
end
