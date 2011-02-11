require 'torquebox/messaging/message_processor'

class SimpleProcessor < TorqueBox::Messaging::MessageProcessor

  def on_message(body)
    File.open( File.join( ENV['BASEDIR'], 'target/messaging-touchfile.txt' ), 'w' ) do |f|
      f.puts( "#{body[:tstamp]} // #{body[:cheese]}" )
    end
  end

end
