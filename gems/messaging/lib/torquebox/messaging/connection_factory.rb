
module TorqueBox
  module Messaging
    class ConnectionFactory

      attr_reader :internal_connection_factory

      def initialize(internal_connection_factory = null)
        @internal_connection_factory = internal_connection_factory
        @hornetq_direct = false
      end

      def with_new_connection(&block)
        connection = create_connection
        connection.start
        begin
          result = block.call( connection )
        ensure
          connection.close
        end
        return result
      end

      def create_connection()
        if !@internal_connection_factory
          # try to connect to HornetQ directly - this currently
          # assumes localhost, and the default AS7 HQ Netty port of 5445
          connect_opts = { org.hornetq.core.remoting.impl.netty.TransportConstants::PORT_PROP_NAME => 5445 }
          transport_config =
            org.hornetq.api.core.TransportConfiguration.new("org.hornetq.core.remoting.impl.netty.NettyConnectorFactory", 
                                                            connect_opts)
          @internal_connection_factory =
            org.hornetq.api.jms.HornetQJMSClient.createConnectionFactory( transport_config )
          @hornetq_direct = true
        end
        
        Connection.new( @internal_connection_factory.create_connection, @hornetq_direct )
      end


      def to_s
        "[ConnectionFactory: internal_connection_factory=#{internal_connection_factory}]"
      end

    end
  end
end
