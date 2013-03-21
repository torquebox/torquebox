require 'torquebox-messaging'


class SimpleProcessor < TorqueBox::Messaging::MessageProcessor

  def on_message(body)
    puts "messaging-alacart SimpleProcessor#on_message()"
    puts ENV.inspect
    basedir = ENV['BASEDIR']
    basedir = basedir.gsub( %r(\\:), ':' )
    basedir.gsub!( %r(\\\\), '\\' )
    $stderr.puts "BASEDIR #{basedir}"
    touchfile = File.join( basedir,  'target', 'messaging-touchfile.txt' )
    File.open( touchfile, 'w' ) do |f|
      f.puts( "#{body[:tstamp]} // #{body[:cheese]}" )
    end

    queue = TorqueBox.fetch( '/queue/backchannel' )
    queue.publish('release')
  end

end
