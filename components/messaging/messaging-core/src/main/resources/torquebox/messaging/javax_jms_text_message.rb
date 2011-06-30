require 'base64'

module javax.jms::TextMessage

  def encode message
    self.text = unless message.nil?
                  marshalled = Marshal.dump( message )
                  Base64.encode64( marshalled )
                end
  end

  def decode
    unless self.text.nil?
      serialized = Base64.decode64( self.text )
      Marshal.restore( serialized )
    end
  end

end
