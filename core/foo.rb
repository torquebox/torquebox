class MessageListener

  def initialize(code)
    @code = code
  end

  def handle(message)
    eval @code
  end
end

l = MessageListener.new( 'puts "the message is #{message}"' )
l.handle( "FOO" )
