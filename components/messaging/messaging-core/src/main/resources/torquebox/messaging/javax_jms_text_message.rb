require 'base64'

module javax.jms::TextMessage

  def encode message
    if message.is_a? String
      self.text = message
    else
      self.set_string_property( 'torquebox_encoding', 'base64' )
      marshalled = Marshal.dump( message )
      encoded = Base64.encode64( marshalled )
      self.text = encoded
    end
  end

  def decode
    if self.get_string_property( 'torquebox_encoding' ) == 'base64'
      serialized = Base64.decode64( self.text )
      Marshal.restore( serialized )
    else
      self.text
    end
  end

end
