require 'torquebox-messaging'


class SimpleProcessor < TorqueBox::Messaging::MessageProcessor

  include TorqueBox::Injectors

  def on_message(body)
    puts ENV.inspect
    basedir = ENV['BASEDIR']
    basedir = basedir.gsub( %r(\\:), ':' )
    basedir.gsub!( %r(\\\\), '\\' )
    $stderr.puts "BASEDIR #{basedir}"
    touchfile = File.join( basedir,  'target', 'messaging-touchfile.txt' )
    File.open( touchfile, 'w' ) do |f|
      f.puts( "#{body[:tstamp]} // #{body[:cheese]}" )
    end

    queue = inject( 'queue/backchannel' )
    queue.publish('release')
  end

end
