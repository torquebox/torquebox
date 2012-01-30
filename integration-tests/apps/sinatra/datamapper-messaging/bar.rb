require 'data_mapper'
require 'dm-sqlite-adapter'

DataMapper::Logger.new($stdout, :debug)
DataMapper::Model.raise_on_save_failure = true 
DataMapper.setup(:default, 'sqlite:///tmp/dm-messaging-test.db')


class Bar
  include DataMapper::Resource
  include TorqueBox::Messaging::Backgroundable

  property :id, Serial

  def bar( message )
    File.open( ENV['BAR_FILE'], 'w' ) do |f|
      f.puts( message )
    end
  end

  def _dump( level )
    [id, self.class].join(':')
  end

  def self._load( string )
    id, clazz = string.split(':')
    raise "Resource not found for #{clazz} with ID #{id}" unless Kernel.const_get(clazz).get(id)
  end
end


DataMapper.auto_upgrade!
DataMapper.finalize
