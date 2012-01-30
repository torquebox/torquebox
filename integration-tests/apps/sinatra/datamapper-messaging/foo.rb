class Foo
  include DataMapper::Resource
  include TorqueBox::Messaging::Backgroundable

  always_background :foo
  property :id, Serial

  def foo( message )
    File.open( ENV['FOO_FILE'], 'w' ) do |f|
      f.puts( message )
    end
  end

end

