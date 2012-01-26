require 'torquebox/messaging/datamapper_marshaling'
require 'dm-core'

class Bar
  include DataMapper::Resource
  include TorqueBox::Messaging::Backgroundable

  property :id, Serial

  def bar( message )
    File.open( ENV['BAR_FILE'], 'w' ) do |f|
      f.puts( message )
    end
  end
end


