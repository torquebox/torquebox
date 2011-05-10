require 'torquebox-messaging'


class SimpleProcessor < TorqueBox::Messaging::MessageProcessor

  def on_message(body)
    basedir = ENV['BASEDIR']
    basedir = basedir.gsub( %r(\\:), ':' )
    basedir.gsub!( %r(\\\\), '\\' )
    $stderr.puts "BASEDIR #{basedir}"
    touchfile = File.join( basedir,  'target', 'messaging-touchfile.txt' )
    File.open( touchfile, 'w' ) do |f|
      f.puts( "#{body[:tstamp]} // #{body[:cheese]}" )
    end
    TorqueBox::Messaging::Queue.new('/queues/backchannel').publish('release')
  end

end
