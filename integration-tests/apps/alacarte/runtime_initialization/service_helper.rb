
class ServiceHelper
  def write_message( message )
    File.open( ENV['TOUCHFILE'], 'w' ) do |f|
      f.puts( message )
    end
  end
end
