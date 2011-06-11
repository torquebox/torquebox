module TorqueBox
  module WebSockets

    class Processor

      attr_accessor :channel
      attr_accessor :session

      def initialize()
        @session = nil
        @channel = nil
      end

      def send(msg,&block)
        frame = org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame.new( msg )
        future = channel.write( frame )
        future.add_listener( BlockListener.new( &block ) ) if block
      end

      def close()
        close_packet = [ 0xFF, 0x00 ]
        buffer = org.jboss.netty.buffer.ChannelBuffers.buffer(2)
        buffer.writeByte( 0xFF )
        buffer.writeByte( 0x00 )
        channel.write( buffer )
      end

    end

    class BlockListener

      include org.jboss.netty.channel.ChannelFutureListener

      def initialize(&block)
        @block = block
      end

      def operationComplete(future)
        @block.call()
      end
  
    end
  end
end
