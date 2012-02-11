require 'data_mapper'
require 'dm-sqlite-adapter'

DataMapper::Logger.new($stdout, :debug)
DataMapper::Model.raise_on_save_failure = true 
DataMapper.setup(:default, 'sqlite:///tmp/dm-messaging-test.db')

class Foo
  include DataMapper::Resource
  include TorqueBox::Messaging::Backgroundable
  include TorqueBox::Messaging::DataMapper
  
  property :id, Serial
  
  def foo( message )
    queue = TorqueBox::Messaging::Queue.new( '/queue/backchannel' )
    queue.publish( message )
    $stderr.puts "foo CALLED"
  end

  # a bug in dm-core prevents us from calling always_background
  # before the method is defined - see https://github.com/datamapper/dm-core/pull/182
  always_background :foo

end


class Bar
  include DataMapper::Resource
  include TorqueBox::Messaging::Backgroundable
  include TorqueBox::Messaging::DataMapper
  
  property :id, Serial

  def bar( message )
    queue = TorqueBox::Messaging::Queue.new( '/queue/backchannel' )
    queue.publish( message )
    $stderr.puts "bar CALLED"
  end

end


DataMapper.auto_upgrade!
DataMapper.finalize
