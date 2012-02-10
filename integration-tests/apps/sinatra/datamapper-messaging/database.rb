require 'data_mapper'
require 'dm-sqlite-adapter'

DataMapper::Logger.new($stdout, :debug)
DataMapper::Model.raise_on_save_failure = true 
DataMapper.setup(:default, 'sqlite:///tmp/dm-messaging-test.db')

class Foo
  include DataMapper::Resource
  include TorqueBox::Messaging::Backgroundable
  include TorqueBox::Messaging::DataMapper

  always_background :foo
  property :id, Serial

  def foo( message )
    File.open( ENV['FOO_FILE'], 'w' ) do |f|
      f.puts( message )
    end
  end
end


class Bar
  include DataMapper::Resource
  include TorqueBox::Messaging::Backgroundable
  include TorqueBox::Messaging::DataMapper

  property :id, Serial

  def bar( message )
    File.open( ENV['BAR_FILE'], 'w' ) do |f|
      f.puts( message )
    end
  end

end


DataMapper.auto_upgrade!
DataMapper.finalize
